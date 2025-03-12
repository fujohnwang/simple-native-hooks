package org.simplenativehooks;

/**
 * {{{
 * _                                    _
 * | |                                  | |
 * | | __   ___    ___  __   __   ___   | |
 * | |/ /  / _ \  / _ \ \ \ / /  / _ \  | |
 * |   <  |  __/ |  __/  \ V /  | (_) | | |
 * |_|\_\  \___|  \___|   \_/    \___/  |_|
 * }}}
 * <p>
 * KEEp eVOLution!
 *
 * @author fq@keevol.cn
 * @since 2017.5.12
 * <p>
 * Copyright 2017 © 杭州福强科技有限公司版权所有
 * [[https://www.keevol.cn]]
 */
public class Sandbox {
    public static void main(String[] args) {
        System.out.println(ClassLoader.getSystemResource("org/simplenativehooks/osx/nativecontent/RepeatHook.out"));
        System.out.println(ClassLoader.getSystemResource("/org/simplenativehooks/osx/nativecontent/RepeatHook.out"));
        System.out.println(ClassLoader.getSystemClassLoader().getResource("org/simplenativehooks/osx/nativecontent/RepeatHook.out"));
        // /Users/fq/workspace.jfx/SimpleNativeHooks/src/main/java                /org/simplenativehooks/osx/nativecontent/RepeatHook.out
    }
}
