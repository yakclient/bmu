package net.yakclient.mixin.base.registry

import net.yakclient.mixin.api.Injection
import net.yakclient.mixin.api.METHOD_SELF
import net.yakclient.mixin.api.Mixer
import net.yakclient.mixin.base.internal.bytecode.ByteCodeUtils.primitiveType
import net.yakclient.mixin.base.internal.loader.ClassTarget
import net.yakclient.mixin.base.internal.loader.ContextPoolManager.applyTarget
import net.yakclient.mixin.base.internal.loader.ContextPoolManager.loadClass
import net.yakclient.mixin.base.registry.pool.ExternalLibRegistryPool
import net.yakclient.mixin.base.registry.pool.MixinMetaData
import net.yakclient.mixin.base.registry.pool.MixinRegistryPool
import java.lang.reflect.Method
import java.net.URL

class MixinRegistry private constructor(private val configuration: RegistryConfigurator.Configuration) {
    private val libRegistry: ExternalLibRegistryPool = ExternalLibRegistryPool()
    private val mixinRegistry: MixinRegistryPool = MixinRegistryPool()

    //TODO there needs to be a better way to have the mixin pool get dumped. Currently nothing will happen if one of our calls is not invoked. A solution could be spawning off another thread and then having a callback? For external libs its very simple and will just go when its needed.
    //TODO there are alot of issues with class reloading for example if a mixin class gets loaded(which it by definition has to) then it loads all of the classes it might reference in parameters etc. so we could fix this by not allowing the loading of mixin classes? that might be an option.
    /**
     * Registers a Mixin from the given class.
     *
     *
     * Right now it doest make sense to return a pointer after adding the mixin.
     * there would be virtually nothing you would need to do with it and accessing
     * the class would already be provided by calling the default classloader etc.
     *
     * @param cls The class to take mixins from
     * @return MixinRegistry for easy access.
     */
    @Throws(ClassNotFoundException::class)
    fun registerMixin(cls: Class<*>): MixinRegistry {
        val data = data(cls)
        for (datum in data) mixinRegistry.pool(datum)
        return this
    }

    @Throws(ClassNotFoundException::class)
    fun registerMixin(cls: Class<*>, proxy: FunctionalProxy): MixinRegistry {
        val data = data(cls)
        for (datum in data) mixinRegistry.pool(datum, proxy)
        return this
    }

    @Throws(ClassNotFoundException::class)
    fun registerLib(url: URL) {
        this.libRegistry.pool(url)
    }

    private fun validateMixin(cls: String) {
        RegistryConfigurator.safeTarget(configuration, ClassTarget.of(cls).toPackage())
    }

    private fun data(cls: Class<*>): Set<MixinMetaData> {
        require(cls.isAnnotationPresent(Mixer::class.java)) { "Mixins must be annotated with @Mixer" }
        val type = cls.getAnnotation(Mixer::class.java).value
        validateMixin(type)
        val mixins = HashSet<MixinMetaData>()
        for (method in cls.declaredMethods) {
            if (method.isAnnotationPresent(Injection::class.java)) {
                val injection = method.getAnnotation(Injection::class.java)
                val methodTo = mixinToMethodName(method)
                require(
                    declaredMethod(
                        type,
                        methodTo,
                        *method.parameterTypes
                    )
                ) { "Failed to find a appropriate method to mix to" }
                mixins.add(
                    MixinMetaData(
                        cls.name,
                        byteCodeSignature(method),
                        type,
                        if (injection.to == METHOD_SELF) byteCodeSignature(method) else methodTo,
                        injection.type,
                        injection.priority
                    )
                )
            }
        }
        return mixins
    }

    private fun byteCodeSignature(method: Method, name: String = method.name): String {
        val methodReturn = method.returnType
        val builder = StringBuilder(name)
        builder.append('(')
        for (type in method.parameterTypes) {
            builder.append(
                if (type.isPrimitive) primitiveType(type) else if (type.isArray) type.name else "L" + type.name.replace(
                    '.',
                    '/'
                ) + ";"
            )
        }
        builder.append(')')
        builder.append(if (methodReturn.isPrimitive) primitiveType(methodReturn) else methodReturn.name)
        return builder.toString()
    }

    private fun mixinToMethodName(method: Method): String {
        if (!method.isAnnotationPresent(Injection::class.java)) return method.name
        val injection = method.getAnnotation(Injection::class.java)
        return if (injection.to == METHOD_SELF) method.name else injection.to
    }

    private fun declaredMethod(cls: String, method: String, vararg parameterTypes: Class<*>): Boolean {
        //TODO Might wanna replace with ASM
        return true
    }

    fun dumpAll() {
        libRegistry.registerAll()
        mixinRegistry.registerAll()
    }

    @Throws(ClassNotFoundException::class)
    fun retrieveClass(cls: String): Class<*> {
        return loadClass(cls)
    }

    companion object {
        fun create(configuration: RegistryConfigurator.Configuration): MixinRegistry {
            for (target in configuration.targets) applyTarget(target)
            return MixinRegistry(configuration)
        }
    }


}