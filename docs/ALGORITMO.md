# Cómo funciona el algoritmo

Este documento explica el algoritmo de ramificación y poda tal como está implementado en [`Game.java`](../src/main/java/Game.java), con un ejemplo seguido paso a paso.

## El problema

Hay *n* agentes y *n* tareas. La matriz de costes `C` indica cuánto tarda cada agente en cada tarea: `C[i][j]` es el tiempo del agente *i* en la tarea *j*. Hay que asignar cada tarea a un agente distinto de forma que la suma de tiempos sea mínima.

Hay *n*! asignaciones posibles (24 con 4 agentes, más de 3,6 millones con 10), así que probarlas todas deja de ser viable enseguida.

## La idea

El algoritmo construye las asignaciones tarea a tarea: primero decide quién hace la tarea 1, luego la 2, y así sucesivamente. Eso forma un árbol en el que cada nodo es una asignación parcial.

En lugar de recorrer el árbol entero, para cada nodo calcula entre qué valores estará el coste de cualquier solución que salga de él, y descarta los nodos que ya no pueden mejorar lo que se tiene.

### Qué guarda cada nodo

| Dato | Significado |
| --- | --- |
| `agents` | En la posición *i*, el agente asignado a la tarea *i*. |
| `assigned` | Qué agentes están ya ocupados. |
| `lastOrder` | Cuántas tareas están ya asignadas. |
| `cost` | Suma de tiempos de las tareas asignadas. |
| `optimalEstimate` | Estimación optimista del coste total. |

### Las dos estimaciones

- **Optimista** (cota inferior): el coste del nodo más, por cada tarea pendiente, el menor tiempo de esa tarea entre todos los agentes. Ninguna solución que salga del nodo puede costar menos.
- **Pesimista** (cota superior): el coste del nodo más, por cada tarea pendiente, el mayor tiempo de esa tarea entre todos los agentes. Cualquier forma de completar el nodo cuesta como mucho eso.

Las dos se calculan mirando todos los agentes, también los ya ocupados. Eso las hace menos ajustadas, pero siguen siendo cotas válidas.

### La cota de poda

La cota es el mejor coste que se sabe que se puede conseguir:

- Al empezar, la estimación pesimista de la raíz.
- Cada vez que se añade un nodo, baja hasta su estimación pesimista si es menor: ese nodo garantiza una solución de ese coste como mucho.
- Cuando aparece una solución completa mejor que la anterior, baja hasta su coste.

Un nodo cuya estimación optimista supera la cota no puede llevar a la mejor solución, y se descarta.

### El orden de exploración

Los nodos pendientes esperan en un montículo de mínimos ordenado por estimación optimista, así que siempre se expande el más prometedor. Si dos nodos empatan, va primero el que tiene más tareas asignadas, porque está más cerca de una solución completa.

Como el montículo entrega los nodos de menor a mayor estimación, en cuanto el nodo extraído ya no puede mejorar la mejor solución, ninguno de los que quedan puede hacerlo y la búsqueda termina.

## El algoritmo, en pseudocódigo

```
insertar la raíz en el montículo
cota ← estimación pesimista de la raíz

mientras el montículo no esté vacío:
    nodo ← extraer el de menor estimación optimista
    si optimista(nodo) > cota o optimista(nodo) ≥ mejor coste:
        terminar

    para cada agente libre en nodo:
        hijo ← nodo + (siguiente tarea → agente)

        si hijo asigna todas las tareas:
            si coste(hijo) < mejor coste:
                mejor solución ← hijo
                cota ← mínimo(cota, coste(hijo))
        si no:
            si optimista(hijo) ≤ cota y optimista(hijo) < mejor coste:
                insertar hijo en el montículo
                cota ← mínimo(cota, pesimista(hijo))
```

## Ejemplo paso a paso

Con la matriz de `ejemplos/ejemplo-4x4.txt`:

| | Tarea 1 | Tarea 2 | Tarea 3 | Tarea 4 |
| --- | --- | --- | --- | --- |
| **Agente 1** | 11 | 12 | 18 | 40 |
| **Agente 2** | 14 | 15 | 13 | 22 |
| **Agente 3** | 11 | 17 | 19 | 23 |
| **Agente 4** | 17 | 14 | 20 | 28 |

Los mínimos por tarea son 11, 12, 13 y 22, que suman 58: la estimación optimista de la raíz. Los máximos son 17, 17, 20 y 40, que suman 94: la cota inicial.

Esta es la traza que produce `tareas -t ejemplos/ejemplo-4x4.txt`. En `agentes [3 1 - -]` el agente 3 hace la tarea 1, el agente 1 la tarea 2, y las tareas 3 y 4 están sin asignar.

```
Nodo raíz: agentes [- - - -], coste 0, estimación optimista 58, cota 94
Nodo añadido: agentes [1 - - -], coste 11, estimación optimista 58, cota 88
Nodo añadido: agentes [2 - - -], coste 14, estimación optimista 61, cota 88
Nodo añadido: agentes [3 - - -], coste 11, estimación optimista 58, cota 88
Nodo añadido: agentes [4 - - -], coste 17, estimación optimista 64, cota 88
Nodo añadido: agentes [1 2 - -], coste 26, estimación optimista 61, cota 86
Nodo añadido: agentes [1 3 - -], coste 28, estimación optimista 63, cota 86
Nodo añadido: agentes [1 4 - -], coste 25, estimación optimista 60, cota 85
Nodo añadido: agentes [3 1 - -], coste 23, estimación optimista 58, cota 83
Nodo añadido: agentes [3 2 - -], coste 26, estimación optimista 61, cota 83
Nodo añadido: agentes [3 4 - -], coste 25, estimación optimista 60, cota 83
Nodo añadido: agentes [3 1 2 -], coste 36, estimación optimista 58, cota 76
Nodo añadido: agentes [3 1 4 -], coste 43, estimación optimista 65, cota 76
Solución que mejora la anterior: agentes [3 1 2 4], coste 64, estimación optimista 64, cota 64
Nodo podado: agentes [3 4 1 -], coste 43, estimación optimista 65, cota 64
Nodo añadido: agentes [3 4 2 -], coste 38, estimación optimista 60, cota 64
Solución descartada: agentes [3 4 2 1], coste 78, estimación optimista 78, cota 64
Nodo añadido: agentes [1 4 2 -], coste 38, estimación optimista 60, cota 64
Nodo podado: agentes [1 4 3 -], coste 44, estimación optimista 66, cota 64
Solución que mejora la anterior: agentes [1 4 2 3], coste 61, estimación optimista 61, cota 61
Nodo podado al extraerlo; fin de la búsqueda: agentes [3 2 - -], coste 26, estimación optimista 61, cota 61

Resumen de la búsqueda:
  Nodos generados: 20
  Nodos expandidos: 9
  Nodos podados: 9
  Soluciones que mejoraron la anterior: 2
  Coste óptimo: 61
```

Lo que ocurre:

1. La raíz genera cuatro hijos, uno por cada agente que puede hacer la tarea 1. La cota baja de 94 a 88 porque el primer hijo garantiza una solución de 88 como mucho.
2. Se expanden los nodos de menor estimación. Al llegar a `[3 1 2 4]` aparece la primera solución completa, de coste 64, y la cota baja a 64.
3. Con la cota en 64 se podan `[3 4 1 -]` y `[1 4 3 -]`, cuyas estimaciones optimistas son 65 y 66.
4. `[1 4 2 3]` cuesta 61 y mejora la solución. La cota baja a 61.
5. El siguiente nodo del montículo tiene estimación optimista 61: ya no puede mejorar la solución, y los que quedan detrás tampoco. La búsqueda termina.

El resultado es agente 1 → tarea 1, agente 4 → tarea 2, agente 2 → tarea 3 y agente 3 → tarea 4, con coste 11 + 14 + 13 + 23 = 61. De las 24 asignaciones posibles solo se han llegado a completar tres.

## Por qué el resultado es óptimo

- La estimación optimista nunca sobreestima, así que un nodo podado por superar la cota no escondía ninguna solución mejor que la cota.
- La cota siempre es alcanzable: o es el coste de una solución ya encontrada, o es la estimación pesimista de un nodo, y todas las soluciones que salen de ese nodo cuestan eso como mucho.
- La búsqueda solo termina cuando ningún nodo pendiente puede mejorar la mejor solución.

Además del razonamiento, las pruebas automáticas comparan el resultado con una búsqueda por fuerza bruta en matrices aleatorias (ver [`GameOptimalityTest.java`](../src/test/java/GameOptimalityTest.java)).

## Coste

En el peor caso el árbol tiene del orden de *n*! nodos, y calcular las estimaciones de cada uno cuesta O(*n*²). La poda no cambia ese peor caso, pero en la práctica deja la mayor parte del árbol sin explorar.

Las operaciones del montículo (insertar y extraer el mínimo) cuestan O(log *m*) con *m* nodos vivos. Con una lista ordenada, insertar costaría O(*m*).

El problema de asignación tiene solución en tiempo polinómico con el algoritmo húngaro, O(*n*³). Ramificación y poda no es la mejor herramienta para este problema concreto: aquí se usa como ejercicio de la técnica.

## Diferencia con vuelta atrás

Vuelta atrás recorre el árbol en profundidad, rama a rama. Ramificación y poda elige en cada paso el nodo más prometedor de todo el árbol y usa las cotas para descartar las ramas que no pueden ser óptimas. A cambio, necesita memoria para guardar todos los nodos vivos.
