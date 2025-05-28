# CustomRx - Кастомная реализация RxJava

Проект представляет собой собственную реализацию основных концепций библиотеки RxJava, включая систему реактивных потоков, операторы преобразования данных, управление потоками выполнения и обработку ошибок.

## Основные функции

- **Реализация базовых компонентов реактивного программирования**:
    - Интерфейс `Observer` с методами `onNext`, `onError`, `onComplete`
    - Класс `Observable` с поддержкой подписки
    - Статический метод `create()` для создания Observable

- **Операторы преобразования данных**:
    - `map` - преобразование элементов потока
    - `filter` - фильтрация элементов по условию
    - `flatMap` - преобразование элементов в новый Observable

- **Управление потоками выполнения**:
    - Интерфейс `Scheduler` с методом `execute`
    - Три реализации Scheduler:
        - `IOScheduler` (аналог Schedulers.io)
        - `ComputationScheduler` (аналог Schedulers.computation)
        - `SingleThreadScheduler` (аналог Schedulers.single)
    - Методы `subscribeOn()` и `observeOn()`

- **Управление подписками и обработка ошибок**:
    - Интерфейс `Disposable` для отмены подписок
    - Механизм `CompositeDisposable` для группового управления
    - Корректная передача ошибок через метод `onError`

## Технологии

- Java 21
- Maven
- JUnit 5 (для тестирования)

## Установка и запуск

1. Склонировать репозиторий:
```bash
git clone 
cd customrx
Собрать проект:

bash
mvn clean install
Запустить демонстрационный пример:

bash
mvn exec:java -Dexec.mainClass="com.customrx.examples.BasicExample"
Структура проекта
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── customrx/
│   │           ├── core/             # Базовые компоненты
│   │           │   ├── Observable.java
│   │           │   ├── Observer.java
│   │           │   ├── Emitter.java
│   │           │   └── ObservableOnSubscribe.java
│   │           ├── schedulers/       # Планировщики
│   │           │   ├── Scheduler.java
│   │           │   ├── IOScheduler.java
│   │           │   ├── ComputationScheduler.java
│   │           │   └── SingleThreadScheduler.java
│   │           ├── operators/        # Операторы
│   │           │   ├── MapOperator.java
│   │           │   ├── FilterOperator.java
│   │           │   └── FlatMapOperator.java
│   │           ├── disposable/       # Управление подписками
│   │           │   ├── Disposable.java
│   │           │   └── CompositeDisposable.java
│   │           └── examples/         # Примеры использования
│   │               └── BasicExample.java
│   └── resources/
├── test/
│   ├── java/
│   │   └── com/
│   │       └── customrx/
│   │           ├── ObservableTest.java
│   │           ├── OperatorsTest.java
│   │           ├── SchedulersTest.java
│   │           └── ErrorHandlingTest.java
└── pom.xml                # Конфигурация Maven

Проект включает комплексные юнит-тесты, проверяющие все ключевые компоненты:

bash
mvn test
Тесты покрывают:

Базовую функциональность Observable

Корректность работы операторов (map, filter, flatMap)

Работу планировщиков в многопоточной среде

Обработку ошибок и отмену подписок

Синхронизацию потоков выполнения

Принципы работы
Архитектура Observable
Observer: получает события из потока

Observable: источник данных, поддерживает цепочки операторов

Операторы: преобразуют или фильтруют данные

Scheduler: управляет потоками выполнения для асинхронной обработки

Disposable: позволяет отменять подписки

Schedulers
IOScheduler:

Для I/O операций

Использует CachedThreadPool

Автоматически масштабируется под нагрузку

Потоки именуются как "io-thread-N"

ComputationScheduler:

Для вычислительных операций

Использует FixedThreadPool с размером = количеству ядер

Потоки именуются как "computation-thread-N"

SingleThreadScheduler:

Все задачи выполняются в одном потоке

Поток именуется как "single-thread-N"

Гарантирует последовательное выполнение задач

Особенности реализации
Потокобезопасная реализация через volatile и синхронизацию

Эффективное управление ресурсами

Автоматическая отмена подписок при ошибках

Подробное логирование потоков выполнения

Комплексная обработка ошибок на всех уровнях

Отчет о тестировании
Тесты подтверждают корректность работы всех компонентов:

Базовые операции Observable работают стабильно

Операторы преобразования данных выполняются корректно

Планировщики гарантируют выполнение в нужных потоках

Механизм Disposable обеспечивает своевременную отмену подписок

Обработка ошибок работает согласно спецификации



