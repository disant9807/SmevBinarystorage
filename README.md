# SmevBinarystorage
Реализует абстракцию хранения, поиска и получения бинарных данных, при необходимости снабженных метаописанием. API хранилища предоставляет возможность получить свойства бинарных данных по идентификатору, получить запрошенный фрагмент бинарных данных с указанной начальной позиции нужного размера.

*Java 1.8 JDK openlogic-openjdk-8u372-b07 Spring 5.3.28*

Точка входа в приложение - [Вот она](https://github.com/disant9807/SmevBinarystorage/blob/master/server/src/main/java/ru/spandco/binstorage/server/ServerApplication.java)

### Модули
- Logic Server - Логика сервиса. Сборка Maven в рабочей директории binaryStorage(root) *clean compile package -Dmaven.test.skip*
- binaryStorageModel - Отдельный артифакт мавен для использования моделей сервиса BinaryStorage в других проектах. Сборка Maven в директории BinaryStorageModel *clean compile package -Dmaven.test.skip*
- binStorageProxy - Модуль прокси для связи в других сервисах с текущим. Сборка Maven в директории binStorageProxy *clean compile package install -Dmaven.test.skip*

### Сборка
Выполнить команду мавен *clean compile package -Dmaven.test.skip* в директории root. После этого запускать server-internal.jar, а после открыть в браузере swagger-api по адресу **http://localhost:8001/swagger-ui/index.html**
