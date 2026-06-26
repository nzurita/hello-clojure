Fase 1: backend "Hola Mundo" en Clojure puro (sin deps), validar toolchain
Fase 2: añadir servidor HTTP al backend (responder en /api/hello)
Fase 3: frontend "Hola Mundo" en ClojureScript con shadow-cljs
Fase 4: conectar front ↔ back vía nginx (fetch /api/hello)
Fase 5: integrar servicios en cmd.sh / arranque automático
Fase 6: crear módulo common/ (CLJC) con schema Malli + :local/root en backend
Fase 7: consumir common/ desde el frontend (validación compartida cliente+servidor)
Fase 8: Mini RPC en el backend
Fase 9: Pantalla demo con React (Rumext)

# Librería de documentación de funciones

```
(require '[clojure.repl :refer :all])
```
Ejemplos:
```
(doc println)
(apropos "+")
(source identity)
```

# Mini-glosario de interop ClojureScript <-> JavaScript

|-----------------|-----------------|-------------------------------------------------|
| ClojureScript   | JavaScript      | Qué es                                          |
|-----------------|-----------------|-------------------------------------------------|
| js/fetch        | fetch           | acceso a un global JS (prefijo js/)             |
| (.then p cb)    | p.then(cb)      | llamar a un método (punto delante)              |
| (.-status resp) | resp.status     | leer una propiedad (punto-guion)                |
| (fn [x] ...)    | x => ...        | función anónima                                 |
| (str a b)       | a + b (strings) | concatenar/convertir a texto (como el . de PHP) |
|-----------------|-----------------|-------------------------------------------------|

# POO vs Clojure
En Clojure los datos y el comportamiento están separados. No hay clases con estado + métodos dentro.

```
1. El DATO        -> un mapa normal:  {:nombre "..." :email "..." :edad 40}
2. El SCHEMA      -> otro dato:        [:map [:nombre [:string ...]] ...]   (Malli)
3. Las FUNCIONES  -> valida?, errores...  (operan sobre el dato)
```

```
;; Clojure: el dato es "tonto" (un mapa) y las funciones viven aparte
(def p {:nombre "Norberto" :email "n@x.com" :edad 40})
(schema/valida? p)   ; la función recibe el dato, no "pertenece" a él
```

- No hay encapsulación, ni getters/setters, ni herencia, ni $this.
- Los mapas son inmutables: "modificar" devuelve un mapa nuevo.
- Cuando sí necesitas polimorfismo (como interfaces en PHP), Clojure tiene defprotocol/defrecord y defmulti/defmethod, pero son la excepción, no la norma. El 90% es mapas + funciones.

# RPC Demos
## OK
curl -s -X POST http://clojurenz.me/api/rpc/command/saludar-persona \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Norberto","email":"norberto@example.com","edad":40}'

## Validación fallida
curl -s -X POST http://clojurenz.me/api/rpc/command/saludar-persona \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"","email":"mal","edad":-3}'

## Comando que no existe
curl -s -X POST http://clojurenz.me/api/rpc/command/no-existe \
  -H 'Content-Type: application/json' -d '{}'

## Demo
curl -s -X GET http://clojurenz.me/api/app \
  -H 'Content-Type: application/json'

# Llamadas a Java


|-----------------|-------------------|------------------|
|      Task       |       Java        |     Clojure      |
|-----------------|-------------------|------------------|
| Instantiation   | new Widget("foo") | (Widget. "foo")  |
| Instance method | rnd.nextInt()     | (.nextInt rnd)   |
| Instance field  | object.field      | (.-field object) |
| Static method   | Math.sqrt(25)     | (Math/sqrt 25)   |
| Static field    | Math.PI           | Math/PI          |
|-----------------|-------------------|------------------|
