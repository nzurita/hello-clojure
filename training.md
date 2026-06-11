Fase 1: backend "Hola Mundo" en Clojure puro (sin deps), validar toolchain
Fase 2: añadir servidor HTTP al backend (responder en /api/hello)
Fase 3: frontend "Hola Mundo" en ClojureScript con shadow-cljs
Fase 4: conectar front ↔ back vía nginx (fetch /api/hello)
Fase 5: integrar servicios en cmd.sh / arranque automático
Fase 6: crear módulo common/ (CLJC) con schema Malli + :local/root en backend
Fase 7: consumir common/ desde el frontend (validación compartida cliente+servidor)