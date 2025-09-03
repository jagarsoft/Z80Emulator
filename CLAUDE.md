Revisa @src\main\java\com\github\jagarsoft\ZuxApp\modules\debugger\DebuggerModule.java y dime en qué falla la habilitación de los botones del ToolBar tras la ejecucion de la instrucción HALT
  ⎿  Read src\main\java\com\github\jagarsoft\ZuxApp\modules\debugger\DebuggerModule.java (330 lines)

● He revisado el código del DebuggerModule.java y he identificado el problema con la habilitación de botones tras la instrucción HALT:

  El problema principal:

  En la línea 315-316, hay un comentario que indica el fallo exacto:
  running.set(false); // TODO si esta pausado tras un HALT debe de cambiar el estado de los botones
  // FALLA: llegar a HALT y dar RUN

  Análisis detallado:

  1. En el método runLoop() (línea 295-300): Cuando la CPU se detiene por HALT, se publica un ExecutionPausedEvent pero el estado running podría no actualizarse correctamente.
  2. El estado inconsistente: El método pause() (línea 310-317) establece running.set(false) al final, pero esto puede crear una condición de carrera donde:
    - Se publica el evento ExecutionPausedEvent("HALTED")
    - Los botones se actualizan por el evento
    - Pero el estado running aún puede estar en transición
  3. En el método doOneStepAndPublish() (línea 206-212): Cuando detecta HALT, llama a eventBus.publish(new ExecutionPausedEvent("HALTED")) y luego pause(), pero no garantiza que el estado
  running se actualice antes de que los listeners de eventos procesen la actualización de botones.

  La solución sería:

  Asegurar que running.set(false) se ejecute antes de publicar el ExecutionPausedEvent("HALTED") para garantizar que el estado esté consistente cuando los listeners actualicen los botones.

● El problema está en la inconsistencia del estado running cuando se ejecuta HALT, causando que los botones no se habiliten correctamente porque el evento se publica antes de que el estado esté
  completamente actualizado.