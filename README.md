# Hue Harvester
di Tommaso Bellinato | [2032597](https://stem.elearning.unipd.it/user/profile.php?id=3804)

Sviluppo del "**Progetto C**"  per il corso  “Elementi di programmazione di sistemi embedded” (6 CFU, codice INP8085258) edizione 2023-24.

## Specifiche di progetto

Si chiede di implementare un’app per fornire il colore medio rilevato dalla fotocamera.

- Una prima interfaccia dell’app deve mostrare
    1. l’anteprima della fotocamera e
    2. il valore medio calcolato per ciascuna delle tre componenti colore
    3. con aggiornamento in tempo reale
- Una seconda interfaccia deve mostrare per ciascuna delle tre componenti colore
    1. i valori medi degli ultimi cinque minuti
    2. in maniera grafica
    3. con aggiornamento in tempo reale
- Non è richiesto che il calcolo e la registrazione dei dati continuino anche quando l’app è in background, ma nel caso in cui l’app passi in background
    1. l’app stessa non deve bloccarsi e deve rimanere in uno stato consistente.

Le immagini possono essere acquisite mediante la classe [`Camera`](https://developer.android.com/guide/topics/media/camera) anche se è deprecata.

- Quale che sia il metodo di acquisizione utilizzato, si consiglia di
    1. impostare la [dimensione](https://developer.android.com/reference/kotlin/android/hardware/Camera.Parameters#getsupportedpreviewsizes) e il [frame rate](https://developer.android.com/reference/kotlin/android/hardware/Camera.Parameters#getsupportedpreviewfpsrange) dell’anteprima ai valori minimi supportati dalla fotocamera del proprio dispositivo.

Per implementare la visualizzazione dei dati è consentito utilizzare librerie esterne.

## Come l’app è stata implementata

### Funzionalità Principali

1. **Anteprima della Fotocamera**:
    - Visualizzazione in tempo reale dell'anteprima della fotocamera.
2. **Calcolo del Valore Medio per ciascuna Componente Colore**:
    - Calcolo in tempo reale del valore medio dei componenti RGB per ogni frame acquisito.
    - [**OpenCV**](https://opencv.org/links/): libreria utilizzata per l'elaborazione delle immagini che può essere utilizzata per ottimizzare i valori medi dei colori.
3. **Visualizzazione Grafica dei Valori Medi degli Ultimi Cinque Minuti**:
    - Grafico in tempo reale che mostra l'andamento delle componenti colore degli ultimi cinque minuti di acquisizione
    - [**MPAndroidChart**](https://github.com/PhilJay/MPAndroidChart): Una libreria completa per creare grafici in tempo reale su Android. Facile da utilizzare e altamente personalizzabile.

### Funzionalità Secondarie

1. **Supporto lingue**
    - l’applicazione supporta in modo completo le lingue inglese (default) e italiano
2. **Aggiornamento in Tempo Reale delle Interfacce**:
    - Aggiornamento continuo e fluido dei valori e dei grafici durante l'uso dell'app
    - [**`LiveData`**](https://developer.android.com/topic/libraries/architecture/livedata) e [**`ViewModel`**](https://developer.android.com/topic/libraries/architecture/viewmodel) : per la gestione e l'aggiornamento dei dati in modo reattivo ed efficiente
    - [**`Room`**](https://developer.android.com/kotlin/multiplatform/room) : per lo storage di 5 minuti di rilevamento dati
3. **UI Funzionante sia in Modalità Portrait che in Modalità Landscape**:
    - Adattabilità dell'interfaccia utente per funzionare correttamente in entrambe le orientazioni del dispositivo.
    - [**`ConstraintLayout`**](https://developer.android.com/training/constraint-layout): Un layout manager versatile che facilita la creazione di UI responsive e adattabili a diverse dimensioni di schermo e orientazioni.
4. **Consistenza dello Stato dell'App in Background**:
    - Gestione del ciclo di vita dell'app, in modo che non si blocchi e rimanga in uno stato consistente quando va in background.
5. **Impostazione della Dimensione e del Frame Rate dell'Anteprima ai Valori Minimi Supportati**:
    - Configurazione della fotocamera per utilizzare la dimensione di anteprima e il frame rate più bassi supportati dal dispositivo per ridurre il carico di elaborazione.
6. **Ottimizzazione delle Prestazioni**:
    - Implementate tecniche per ottimizzare le prestazioni dell'app, specialmente per quanto riguarda il calcolo dei valori medi e l'aggiornamento del grafico
7. **Utilizzo di View Binding**:
    - L'app utilizza il **View Binding** per interagire con le viste XML in modo sicuro ed efficiente.
    - Il View Binding riduce il rischio di crash causati da errori di tipo `NullPointerException`, migliora la leggibilità del codice e facilita la manutenzione, poiché genera automaticamente le classi di binding per ogni layout XML, eliminando la necessità di chiamare `findViewById()`.

### Considerazioni Finali

L'uso di View Binding nel progetto garantisce che le interazioni con le viste siano sicure e prive di errori di runtime, migliorando al contempo la leggibilità e la manutenibilità del codice. Questa pratica, insieme all'uso di altre tecnologie e librerie come `LiveData`, `ViewModel`, `Room` e `ConstraintLayout`, contribuisce a creare un'app robusta e performante.
