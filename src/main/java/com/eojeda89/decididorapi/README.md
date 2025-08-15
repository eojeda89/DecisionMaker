Arquitectura Hexagonal (Ports & Adapters)

Estructura de paquetes creada:

- adapter/
  - in/
    - web/          (controladores REST)
    - cli/          (entradas por consola)
    - messaging/    (suscriptores de eventos/colas)
  - out/
    - persistence/  (repositorios, JPA, etc.)
    - messaging/    (publicadores de eventos)
    - external/     (integraciones con APIs externas)

- application/
  - port/
    - in/           (interfaces de casos de uso)
    - out/          (interfaces de gateways/repositorios)
  - service/        (implementaciones de casos de uso, orquestación)

- domain/
  - model/          (entidades, value objects, agregados)
  - service/        (servicios de dominio puros)
  - event/          (eventos de dominio)

- common/
  - exception/      (excepciones comunes)
  - util/           (utilidades transversales)

- configuration/    (configuración Spring/Beans)

Notas:
- Mantener la dependencia dirigida hacia dentro: adapters -> application -> domain.
- Evitar que domain dependa de frameworks.
- Los servicios de application dependen de ports (interfaces) y reciben/invocan adapters por inversión de control (DI).