package net.yakclient.apitests;


import net.questcraft.apitests.MixinTestCase;
import net.yakclient.mixin.internal.loader.PackageTarget;
import net.yakclient.mixin.registry.MixinRegistry;
import net.yakclient.mixin.registry.RegistryConfigurator;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RegistryTest {
    //for runtime -Djava.system.class.loader=net.yakclient.mixin.internal.loader.ProxyClassLoader

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {

        final RegistryConfigurator configure = RegistryConfigurator.configure().addSafePackage("net.questcraft").addTarget(PackageTarget.create("net.questcraft"));
        final MixinRegistry mixinRegistry = configure.create();
        {
            mixinRegistry.registerMixin(net.questcraft.apitests.MixinTestCase.class).dumpAll();

            final Class<?> aClass = mixinRegistry.retrieveClass(net.questcraft.apitests.MixinSourceClassTest.class.getName()); //target.retrieveClass(MixinSourceClassTest.class.getName());
            final Constructor<?> constructor = aClass.getConstructor(String.class);
            final Object obj = constructor.newInstance("YAY");
            obj.getClass().getMethod("printTheString", int.class).invoke(obj,11);
//            new Label().toString();
        }

//        {
//            mixinRegistry.registerMixin(net.questcraft.apitests.SecondMixinTestCase.class, (cancel -> {
//                System.out.println("Proxied");
//            })).dumpAll();
//
//
//
//            final Class<?> aClass = mixinRegistry.retrieveClass(net.questcraft.apitests.MixinSourceClassTest.class.getName());
//
//            final Constructor<?> constructor = aClass.getConstructor(String.class);
//            final Object obj = constructor.newInstance("YAY");
//            aClass.getMethod("printTheString", int.class).invoke(obj, 10);
//        }

    }

    @Test
    public void testMixinRegistry() throws ClassNotFoundException {
        System.out.println("GOt here");
        RegistryConfigurator.configure().create().registerMixin(MixinTestCase.class).dumpAll();
        printSomething();
    }


    public void printSomething() {
        System.out.println("Something");
    }
}
