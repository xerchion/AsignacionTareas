# Asignación de tareas por ramificación y poda

[![Tests](https://github.com/xerchion/AsignacionTareas/actions/workflows/tests.yml/badge.svg)](https://github.com/xerchion/AsignacionTareas/actions/workflows/tests.yml)

Programa de línea de comandos en Java que resuelve el problema de asignación: hay *n* agentes y *n* tareas, cada agente tarda un tiempo distinto en cada tarea, y hay que dar una tarea a cada agente de forma que el tiempo total sea el menor posible.

Lo resuelve con un algoritmo de **ramificación y poda** sobre un montículo de mínimos, con todas las estructuras de datos (montículo, matriz, vectores) escritas a mano. Nació como práctica de la asignatura *Programación y Estructuras de Datos Avanzadas* (Grado en Ingeniería Informática, UNED).

## Ejemplo

Entrada (`ejemplos/ejemplo-4x4.txt`): la primera línea da el número de agentes y de tareas; cada línea siguiente, los tiempos de un agente en cada tarea.

```
4 4
11 12 18 40
14 15 13 22
11 17 19 23
17 14 20 28
```

Salida: una línea `agente tarea` por cada tarea y el coste total.

```
Resultado Óptimo
 (agente/tarea)
1 1
4 2
2 3
3 4
Coste total: 61
```

Asignar a cada agente la tarea de su mismo número costaría 73. De las 24 asignaciones posibles, el algoritmo solo llega a completar tres.

## Cómo funciona

- Cada nodo del árbol de búsqueda es una asignación parcial: las *k* primeras tareas ya tienen agente.
- Para cada nodo se calcula una **estimación optimista** (su coste más el menor tiempo de cada tarea pendiente) y una **estimación pesimista** (su coste más el mayor tiempo de cada tarea pendiente).
- Los nodos vivos esperan en un montículo de mínimos ordenado por estimación optimista, así que siempre se expande el más prometedor.
- La cota de poda es la menor estimación pesimista vista hasta el momento o, cuando ya hay una, el coste de la mejor solución completa. Todo nodo cuya estimación optimista supere la cota se descarta sin explorarlo.

La explicación completa, con la traza del ejemplo paso a paso, está en [docs/ALGORITMO.md](docs/ALGORITMO.md).

## Uso

Requisitos: Java 11 o superior y Maven.

```bash
mvn compile
java -cp target/classes tareas ejemplos/ejemplo-4x4.txt
```

Sintaxis:

```
tareas [-t] [-h] [fichero entrada] [fichero salida]
```

| Argumento | Efecto |
| --- | --- |
| `-t` | Añade la traza del algoritmo: cada nodo que se añade, se poda o mejora la solución. |
| `-h` | Muestra la ayuda. |
| `fichero entrada` | Fichero con el problema. Si se omite, los datos se piden por teclado. |
| `fichero salida` | Fichero donde guardar el resultado. Si se omite, se muestra por pantalla. |

Los tiempos deben ser enteros no negativos y la matriz, cuadrada.

En Windows, si las tildes no se ven bien en la consola, ejecuta antes `chcp 65001` y añade `-Dfile.encoding=UTF-8` al comando `java`.

## Pruebas

```bash
mvn test
```

Hay más de 50 pruebas con JUnit 5 y [jqwik](https://jqwik.net/) (pruebas basadas en propiedades). Las más importantes comprueban el resultado del algoritmo contra una búsqueda por fuerza bruta: en cada ejecución se generan 1.000 matrices aleatorias de hasta 7×7, la mitad con muchos costes repetidos para forzar empates, y el coste devuelto tiene que coincidir con el mínimo real.

Las pruebas se ejecutan también en GitHub Actions con cada cambio.

## Estructura

| Clase | Papel |
| --- | --- |
| `tareas` | Punto de entrada. |
| `TaskAssignmentSystem` | Coordina la ejecución: carga de datos, algoritmo, resultados y errores. |
| `Game` | El algoritmo de ramificación y poda. |
| `Node` | Nodo del árbol: asignación parcial, coste y estimación optimista. |
| `Heap` | Montículo binario de mínimos, genérico. |
| `GameData` | Datos del problema: matriz de tiempos, agentes y tareas. |
| `Matrix`, `Vector`, `VectorB` | Matriz y vectores con índices desde 1. |
| `UserInterface`, `ArgsHandler`, `FileController` | Argumentos, entrada por teclado, lectura y escritura de ficheros. |
| `Constants` | Textos y límites. |

## Límites

El problema de asignación se puede resolver en tiempo polinómico con el algoritmo húngaro. Este proyecto no pretende competir con él: es un ejercicio de la técnica de ramificación y poda, cuyo coste en el peor caso es factorial.

Como orden de magnitud, con tiempos aleatorios entre 1 y 100 un problema de 14×14 se resuelve en décimas de segundo y uno de 20×20 en pocos segundos. A partir de ahí el tiempo y la memoria crecen deprisa. El programa admite hasta 50 agentes y corta la ejecución a los 5 minutos.

## Autor

Sergio Ucedo · [Portafolio](https://xerchion.github.io/webPortfolio/) · [LinkedIn](https://www.linkedin.com/in/sergio-ucedo)
