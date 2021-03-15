package net.yakclient.apitests;

import net.yakclient.mixin.api.Injection;
import net.yakclient.mixin.api.InjectionType;
import net.yakclient.mixin.api.Mixer;

@Mixer("net.yakclient.apitests.MixinSourceClassTest")
public class SecondMixinTestCase {
    @Injection(type = InjectionType.BEFORE_INVOKE)
    public void printTheString() {
        System.out.println("I also wanna print this");
    }
}
