# Enunciado original — transacciones JDBC

Fuente: `EJERCICIOS Transacciones.pdf` del módulo **Acceso a Datos**.

## Preparación

Importa la base de datos **`pedidos.sql`** y analiza la estructura de sus tablas y relaciones.

## Ejercicio 1

Teniendo en cuenta los conceptos de transacciones, se deben insertar en **una única transacción** los pedidos y las líneas del mismo pedido.

Crea un programa que permita introducir la información de un pedido (su cabecera) y después sus líneas de pedido hasta que se teclee un código de artículo igual a `*`.

Cuando el usuario teclee `*`, se finaliza la introducción de datos del pedido y se pregunta:

> ¿Desea grabar el pedido (S/N)?

- Si la respuesta es negativa, se deben deshacer todas las inserciones realizadas.
- Si la respuesta es afirmativa, se debe grabar el pedido.

Utiliza los datos de las tablas `clientes` y `articulos` para evitar errores de claves foráneas. El enunciado propone leer la fecha en formato `DD/MM/AAAA` y transformarla a `Date` mediante `SimpleDateFormat`.

## Ejercicio 2

Crea un programa que pida el número de un pedido, muestre todas sus líneas y permita al usuario eliminar alguna de ellas. Durante este proceso **ninguna otra transacción debe poder modificar dichas líneas ni añadir ninguna más**.

## Base de datos

El volcado original requerido por el ejercicio está en [`database/pedidos.sql`](database/pedidos.sql).
