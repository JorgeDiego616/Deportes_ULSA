1. Git / GitHub
¿Qué es un repositorio y qué diferencia hay entre uno local y uno remoto?
¿Por qué no se recomienda hacer push directo a main?
No se recomienda debido a que podemos generar errores de sobreescritura donde hacemos cambios en lugares donde ya hay algo escrito y haciendo un pull request nos aseguramos de no afectar a la rama principal.

¿Qué es una rama y por qué se usa una distinta por cada actividad?
Una rama es como otra línea de trabajo donde tienes todo lo que tiene main y ya cada quien le hace las modificaciones que desea sin afectar main. Se usan distintas ramas para que cada funcionalidad tenga todo un área de trabajo designada y aislada sin afectar a las demás funcionalidades ni a main y así tener un mejor control del proyecto.

¿Qué es un Pull Request y qué problema resuelve?
Es una solicitud que se manda a la rama main explicando todos los cambios que contiene la otra rama respecto a main y sirve para evitar problemas de sobreescritura en el código, comandos duplicados y facilita el que varios integrantes trabajen en varias funcionalidades en diferentes ramas a la vez sin afectarse entre ellos ni a main.

¿Qué puede provocar un conflicto al hacer merge?
Código que se sobreescriba, código que se sabotee entre sí, comandos duplicados, además de crear bugs.

¿Qué evita una rama protegida?
Normalmente se protege main ya que una rama protegida es una rama a la que se ponen reglas normalmente se implementa el pull request, esto evita que ramas importantes como main no se puedan hacer modificadar sin más, solo que se pueda mandar una solicitud de cambio.

2. Herramientas y configuración
¿Qué es un IDE y qué rol cumple Android Studio?
Un IDE es un entorno con el conjunto de herramientas necesarias para el desarrollo de software y el rol que cumple en Android studio es que es que posee las herramientas para que pueda desarrollar una app.

¿Qué hace Gradle dentro de un proyecto Android?
Gradle automatiza la construcción del proyecto Android, administra las dependencias y se encarga de tareas como compilar y generar la aplicación.

¿Qué diferencia hay entre probar en el emulador y en un dispositivo físico?
En un emulador se puede probar sin algún daño real la app ya que es un dispositivo virtual y no corre riesgo probar ahí ya que cualquier cosa se puede reiniciar y listo, mientras que el físico es solo para pruebas finales ya que si hay consecuencias que pueden ser irreversibles.

Si el proyecto compila y corre bien pero el IDE marca todo en rojo, ¿qué deberías hacer primero?
Quiere decir que no hay un error en tu código más bien es en el Gradle sería mejor esperar a que se construya bien tu app o reiniciar el Gradle por si se construyó mal

3. Kotlin
¿Cuál es la diferencia entre var y val?
var: variable cuyo valor puede cambiar después de declararla.
val: variable de solo lectura; no puedes reasignarle otro valor después de inicializarla.

¿Qué partes tiene la anatomía de una función en Kotlin?
Empieza con la palabra fun + nombre_de_función (Parámetros): tipo_retorno {La lógica de la función}

¿Qué significa que una función devuelva Unit?
Indica que una función no regresa un valor significativo.

¿Qué es un parámetro con valor por defecto?
Es un parámetro que si no recibe un valor, toma ese valor por defecto.

¿Qué son los argumentos nombrados y para qué sirven?
Son argumentos que se pasan indicando explícitamente el nombre del parámetro, lo que mejora la claridad y permite identificar fácilmente qué valor corresponde a cada parámetro. 

¿Qué es una función de una sola expresión?
Es una función cuyo resultado se puede expresar con una sola expresión y que puede escribirse usando = en lugar de {} y return. 

¿Qué es una función de orden superior? Da un ejemplo.
Una función de orden superior es aquella que recibe una función como parámetro o devuelve una función como resultado. 

4. Jetpack Compose
¿Qué hace que una función sea un Composable?
La sintaxis de @Composable antes de la función.

¿Qué diferencia hay entre Column, Row y Box?
Column organiza verticalmente, Row horizontalmente y Box permite superponer elementos. 

¿Qué diferencia hay entre Arrangement y Alignment?
Arrangement controla la distribución de los elementos en el eje principal, mientras que Alignment controla su alineación en el eje cruzado. 



¿Para qué sirve un Modifier?
Son modificadores para lo que esté dentro del modifier se puedan cambiar sus propiedades como tamaño, color, etc.

¿Qué provee un Scaffold?
Scaffold proporciona una estructura de pantalla con espacios para elementos comunes como barras superiores, navegación inferior, FAB y contenido principal. 

¿Por qué se necesita remember junto con mutableStateOf?
mutableStateOf crea un estado observable y remember permite conservar ese estado durante las recomposiciones para que no se reinicie. 

¿Qué es una recomposición y qué la dispara?
Una recomposición es la actualización de las funciones Composable cuando cambia un estado del que dependen. Generalmente la dispara un cambio en un estado observable, como mutableStateOf. 

5. Navegación
¿Qué relación hay entre un NavController y un NavHost?
El NavController controla y administra la navegación, mientras que el NavHost contiene y muestra los destinos de navegación. 

¿Qué hace navigate() y qué hace popBackStack()?
navigate() avanza hacia otro destino y popBackStack() regresa al destino anterior. 

¿Para qué sirve popUpTo con inclusive = true?
inclusive = true hace que el destino especificado en popUpTo también sea eliminado de la pila de navegación. 
La idea es evitar que el usuario pueda regresar a ciertas pantallas mediante el botón "Atrás". 

¿Por qué el NavController de los tabs es distinto al NavController raíz de la app?
El NavController raíz controla la navegación general de la aplicación, mientras que el de los tabs controla únicamente las pantallas/secciones internas de los tabs. Son distintos para mantener separadas sus pilas de navegación. 

6. Logger
¿Para quién está pensado el Logger: el desarrollador o el usuario?
Para el desarrollador ya que muestra información que solo sirve para los desarrolladores.

¿Qué diferencia hay entre Log.d, Log.w y Log.e?
d es para depuración, w para advertencias y e para errores. 

¿Qué es el TAG y por qué conviene que sea el nombre de la clase?
El TAG identifica el origen del log y usar el nombre de la clase facilita localizar y filtrar los mensajes. 

¿Por qué no conviene dejar logs de debug activos en producción?
Porque pueden exponer información sensible o interna, generar ruido y afectar el rendimiento; los logs de depuración deben controlarse o eliminarse en producción. 

7. Toasts
¿Para quién está pensado un Toast?
Para el usuario ya que muestra de forma breve información sobre alguna acción que hizo el usuario.

¿Qué pasa si olvidas el .show()?
Literal el Toast no se ve en pantalla.

¿Cuál es la diferencia entre LENGTH_SHORT y LENGTH_LONG?
LENGTH_SHORT muestra el Toast durante menos tiempo y LENGTH_LONG durante más tiempo. 

¿Por qué el Toast debe dispararse dentro de un evento como onClick y no suelto en el cuerpo del Composable?
Porque suelto puede provocar que se active varias veces sin que se necesite, mientras que en el onClick te aseguras de que se active en el momento justo que se requiere.

¿Cuándo NO deberías usar un Toast?
No se usa cuando se quiere presentar mucha información relevante o se necesita que el usuario interactúe con el.

8. SharedPreferences
¿Qué tipo de datos es adecuado guardar en SharedPreferences?
SharedPreferences sirve para guardar pequeños datos simples y persistentes, como configuraciones, preferencias o valores de sesión. 

¿Para qué se usó SharedPreferences en el proyecto de clase?
Se usó para guardar la sesión, nombre de usuario y token del mismo usuario.

¿En qué se diferencia de guardar datos en una base de datos como SQLite?
Que se usa para guardar grandes cantidades de datos, hacer consultas, relaciones y demás, no para datos pequeños como guardar una sesión como en este caso.
