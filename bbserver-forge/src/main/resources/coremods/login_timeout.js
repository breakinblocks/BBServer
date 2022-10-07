function initializeCoreMod() {
    return {
        'login_timeout': {
            // ServerLoginPacketListenerImpl.tick()
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.network.ServerLoginPacketListenerImpl',
                'methodName': 'm_10050_',
                'methodDesc': '()V'
            },
            'transformer': function (method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var IntInsnNode = Java.type('org.objectweb.asm.tree.IntInsnNode');

                var timeoutTicks = ASMAPI.findFirstInstruction(method, Opcodes.SIPUSH);

                while (true) {
                    if (timeoutTicks == null) {
                        ASMAPI.log('WARN', '[login_timout] Failed find the SIPUSH instruction with 600 ticks.');
                        return method;
                    }

                    if (timeoutTicks instanceof IntInsnNode && timeoutTicks.operand === 600) {
                        break;
                    }

                    timeoutTicks = ASMAPI.findFirstInstruction(method, Opcodes.SIPUSH, method.instructions.indexOf(timeoutTicks));
                }

                var hook = ASMAPI.buildMethodCall('com/breakinblocks/bbserver/coremods/LoginTimeoutCoremod',
                    'modifyLoginTimeout',
                    '(I)I',
                    ASMAPI.MethodType.STATIC);
                method.instructions.insert(timeoutTicks, hook);

                ASMAPI.log('INFO', '[login_timout] Patched.');
                return method;
            }
        }
    }
}
