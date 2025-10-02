# Pokemonium

This project is an Android application that displays a list of Pokémon with infinite scrolling. It's built to demonstrate a well-architected Android application using modern technologies, based on a test assignment. The application fetches data from the PokeAPI and caches it locally to support offline viewing and provide a smooth user experience.

## Architecture

The application follows the **MVVM (Model-View-ViewModel)** architecture, which promotes a separation of concerns and improves testability and maintainability.

*   **Model**: Represents the data and business logic layer. It consists of:
    *   **Repository**: The `PokemonRepository` is the single source of truth for the app's data. It is responsible for fetching data from the network and/or the local database and deciding when to refresh the cache.
    *   **Remote Data Source**: `PokemonApiService` (using Retrofit) handles communication with the PokeAPI (`https://pokeapi.co/api/v2/pokemon`).
    *   **Local Data Source**: `PokemonDao` (using Room) manages the local cache of Pokémon data in a SQLite database. This provides offline support and faster loading times.

*   **View**: The UI layer, built entirely with **Jetpack Compose**.
    *   It consists of Composable functions that observe state from the ViewModel and render the UI.
    *   The core UI component is the `InfiniteScrollList`, a reusable Composable that displays the list of Pokémon and triggers more data to be loaded as the user scrolls to the end.

*   **ViewModel**: The `PokemonViewModel` acts as a bridge between the View and the Model (Repository).
    *   It holds the UI state (e.g., the list of Pokémon, loading status, errors) and exposes it to the View via `StateFlow`.
    *   It contains the business logic to fetch data from the repository and handles user interactions. It is completely independent of the Android Framework, making it easy to unit test.

## Core Components & Features

*   **Infinite Scrolling**: Implemented using a custom `InfiniteScrollList` Jetpack Compose component. It observes the scroll position of a `LazyColumn` and triggers a request for the next page of data when the user nears the end of the list.
*   **Caching Strategy**:
    *   Pokémon data fetched from the API is stored in a local Room database.
    *   The repository first attempts to load data from the cache.
    *   When new data is fetched from the network, it updates the cache. A `CacheMetadataEntity` is used to track the total number of items to manage pagination and cache validation.
    *   The app can function in an offline mode, displaying the cached data if a network connection is not available.
*   **Dependency Injection**: **Koin** is used for dependency injection to manage the lifecycle of components like ViewModels, Repositories, and data sources. This makes the codebase more modular and easier to test.
*   **Asynchronous Operations**: **Kotlin Coroutines** are used extensively for managing background threads for network requests and database access, preventing the UI from freezing.
*   **Unit Testing**: The project includes unit tests for the `PokemonViewModel` to demonstrate the testability of the MVVM architecture.

## Libraries Used

*   **Jetpack Compose**: For building the UI.
*   **ViewModel**: To manage UI-related data in a lifecycle-conscious way.
*   **Room**: For local database caching.
*   **Retrofit & OkHttp**: For networking.
*   **Kotlinx.serialization**: For parsing JSON from the API.
*   **Koin**: For dependency injection.
*   **Coil**: For image loading.
*   **Kotlin Coroutines**: For asynchronous programming.
*   **JUnit & Mockito/MockK**: For unit testing.

## How to Build & Run

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Let Gradle sync the dependencies.
4.  Run the `app` configuration on an emulator or a physical device.
