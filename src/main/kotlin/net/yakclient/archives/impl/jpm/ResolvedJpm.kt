package net.yakclient.archives.impl.jpm

import net.yakclient.archives.ResolvedArchive
import net.yakclient.archives.security.PrivilegeList
import java.lang.module.Configuration
import java.lang.module.ModuleDescriptor

public class ResolvedJpm(
    public val module: Module,
) : ResolvedArchive {
    override val classloader: ClassLoader = module.classLoader ?: ClassLoader.getSystemClassLoader()
    override val packages: Set<String> = module.packages
    override val parents: Set<ResolvedArchive>
    public val configuration: Configuration = module.layer.configuration()
    public val layer: ModuleLayer = module.layer
    private val services: Map<String, List<Class<*>>> by lazy {
        module.descriptor.provides().associate { it.service() to it.providers().map(classloader::loadClass) }
    }

    init {
        nameToArchive[module.name] = this

        fun ModuleLayer.allModules(): Set<Module> = modules() + parents().flatMap { it.allModules() }

        fun loadArchive(name: String): ResolvedArchive? =
            nameToArchive[name] ?: module.layer.allModules().find { it.name == name }?.let(::ResolvedJpm)

        parents = run {
            module.descriptor.requires()
                .filterNot {
                    it.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC)
                }.mapTo(HashSet()) {
                    loadArchive(it.name())!!
                }
        }
    }

    private companion object {
        private val nameToArchive: MutableMap<String, ResolvedArchive> = HashMap()
    }

//    override fun loadService(name: String): List<Class<*>> = services[name] ?: ArrayList()
}