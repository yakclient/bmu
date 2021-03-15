package net.yakclient.apitests;

import net.yakclient.mixin.api.Injection;
import net.yakclient.mixin.api.Mixer;
import net.yakclient.mixin.api.Shadow;
import org.jetbrains.annotations.NotNull;

@Mixer("net.yakclient.apitests.MixinSourceClassTest")
public abstract class MixinTestCase {

    @Shadow
    public abstract void shadowMethod();

    public abstract void second();

    public static void staticTest() {
        System.out.println("The current class is this");
    }


    @Injection(to = "printTheString")
    public void overrideIt() {
//        @NotNull String test = "THIS IS THE TEST STRING";
//        staticTest();
//        System.out.println("Print the test string " + test);
        System.out.println("No print this too!");
    }
}
