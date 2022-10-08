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

                ASMAPI.log('INFO', '[login_timout] Patching...');

                var hook = ASMAPI.buildMethodCall('com/breakinblocks/bbserver/coremods/LoginTimeoutCoremod',
                    'modifyLoginTimeout',
                    '(I)I',
                    ASMAPI.MethodType.STATIC);

                var patched = 0;
                var timeoutTicks = ASMAPI.findFirstInstruction(method, Opcodes.SIPUSH);

                while (timeoutTicks != null) {
                    if (timeoutTicks instanceof IntInsnNode && timeoutTicks.operand === 600) {
                        method.instructions.insert(timeoutTicks, hook.clone({}));
                        patched++;
                    }

                    timeoutTicks = ASMAPI.findFirstInstructionAfter(method, Opcodes.SIPUSH, 1 + method.instructions.indexOf(timeoutTicks));
                }

                if (!patched) {
                    ASMAPI.log('WARN', '[login_timout] Failed find any SIPUSH instructions with operand 600.');
                } else {
                    ASMAPI.log('INFO', '[login_timout] Patched ' + patched + ' SIPUSH instruction(s) with operand 600.');
                }

                return method;
            }
        }
    }
}
