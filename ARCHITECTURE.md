# Noct-Journal Architecture Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architectural Patterns](#architectural-patterns)
3. [Component Details](#component-details)
4. [Data Flow](#data-flow)
5. [Algorithm Design](#algorithm-design)
6. [File Format Specification](#file-format-specification)
7. [UML Diagrams](#uml-diagrams)

## System Overview

Noct-Journal employs a multi-layered architecture that separates concerns between presentation, business logic, data management, and persistence. This design enables maintainability, testability, and future extensibility.

### Architectural Layers

```
┌───────────────────────────────────────────────────────────┐
│                  Presentation Layer                       │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐     │
│  │GraphicSchedule│ │  GridPanel   │  │Other Panels │     │
│  │    (Main)    │  │   (Visual)   │  │   (Dialogs) │     │
│  └─────────────┘  └──────────────┘  └─────────────┘     │
└───────────────────────────────────────────────────────────┘
                          │
                          ▼
┌───────────────────────────────────────────────────────────┐
│                 Business Logic Layer                      │
│  ┌──────────────┐          ┌──────────────────┐          │
│  │SchedulePlanner│◄────────┤ SleepAlgorithm   │          │
│  │  (Coordinator)│          │  (Intelligence)  │          │
│  └──────────────┘          └──────────────────┘          │
└───────────────────────────────────────────────────────────┘
                          │
                          ▼
┌───────────────────────────────────────────────────────────┐
│                    Data Layer                             │
│  ┌──────────┐  ┌────────┐  ┌──────────────┐             │
│  │ Schedule │  │ Block  │  │  BlockType   │             │
│  │  (Grid)  │  │(Event) │  │ (Enumeration)│             │
│  └──────────┘  └────────┘  └──────────────┘             │
└───────────────────────────────────────────────────────────┘
                          │
                          ▼
┌───────────────────────────────────────────────────────────┐
│                 Persistence Layer                         │
│  ┌──────────────┐          ┌──────────────┐              │
│  │ScheduleFileIO│          │ByteConverter │              │
│  │   (I/O)      │          │   (Utility)  │              │
│  └──────────────┘          └──────────────┘              │
└───────────────────────────────────────────────────────────┘
```

## Architectural Patterns

### 1. Model-View Architecture
The application separates data models from their visual representation:

- **Model**: `Schedule`, `Block`, `BlockType` - Pure data structures
- **View**: `GraphicSchedule`, `GridPanel`, `GraphicBlock` - Visual components

### 2. Singleton Pattern (Static Utility Classes)
Several components use static methods and state, functioning as singletons:

- **SleepAlgorithm**: Single instance of sleep optimization logic
- **ScheduleFileIO**: Single file I/O manager
- **GraphicSchedule.schedule**: Shared schedule instance

**Rationale**: These components represent application-wide services that don't benefit from multiple instances.

### 3. Strategy Pattern
The sleep algorithm implements two distinct strategies:

- **Extended Sleep Strategy**: Lengthens existing sleep blocks
- **Nap Insertion Strategy**: Adds nap blocks between events

The algorithm can apply one or both strategies based on user preferences.

### 4. Binary Insertion (Algorithm Pattern)
The Schedule class maintains blocks in sorted order using binary insertion:

- Time Complexity: O(log n) for finding insertion point
- Efficient for frequently-searched, rarely-modified collections

### 5. Observer Pattern
GUI components use Java Swing's event listener pattern:

- `MouseListener`: Grid interactions
- `ActionListener`: Button clicks
- `WindowListener`: Application lifecycle events

## Component Details

### Core Components

#### 1. SchedulePlanner
**Location**: `framework/SchedulePlanner.java`

**Responsibilities**:
- Coordinates between GUI, algorithm, and I/O subsystems
- Manages schedule operations (add, remove events)
- Handles application lifecycle (initialization, save, exit)

**Key Methods**:
- `addEvent(Block b)`: Adds event to schedule
- `applyAlgorithm()`: Triggers sleep optimization
- `load()` / `save()`: Persistence operations
- `goToSleep()` / `wakeUp()`: Sleep tracking

**Dependencies**:
- Schedule (composition)
- SleepAlgorithm (delegation)
- ScheduleFileIO (delegation)

#### 2. SleepAlgorithm
**Location**: `framework/info/SleepAlgorithm.java`

**Responsibilities**:
- Implements sleep optimization algorithms
- Manages wake-up time preferences
- Tracks sleep statistics
- Handles block time calculations

**Key Algorithms**:

**addLongerSleep()**:
```
For each sleep block from today onward:
    Calculate missing sleep time
    If missing ≥ 2 hours:
        Extend block by 2 hours (earlier start time)
    Else if missing > 0:
        Extend block by missing time
    Check for overlaps and adjust
```

**addNaps()**:
```
For each day from today onward:
    Find gaps between events
    If gap ≥ 1 hour + 30 min buffer:
        Schedule 1-hour nap
    Else if gap = 45-60 min:
        Schedule gap - 30 min buffer
    Maximum 1 hour naps per day
    Stop when sleep requirement met
```

**Constants**:
- `RECOMMENDED_SLEEP_HOURS`: 56 hours per week
- Time constants: `EIGHT_HOURS`, `TWO_HOURS`, `ONE_HOUR`, `FIFTHTEEN_MINUTES`
- Day constants: `MONDAY` through `SUNDAY` (0-6)
- Stat constants: `SLEEP_NEEDED`, `HOURS_SLEPT`, etc.

#### 3. Schedule
**Location**: `framework/info/grid/Schedule.java`

**Responsibilities**:
- Maintains sorted list of blocks
- Provides block insertion/removal operations
- Implements overlap detection

**Data Structure**:
```java
ArrayList<Block> grid  // Sorted by block start time
```

**Key Methods**:
- `addBlock(Block b)`: Binary insert with overlap check
- `findLocationFor(Block b)`: Binary search for insertion point
- `getBlockAt(Date date)`: Retrieves block at given time
- `getAllBlocksOfType(BlockType type)`: Filters by type

**Overlap Detection**:
Blocks cannot overlap. The `canInsertAt()` method checks:
- Previous block doesn't extend into new block's time
- New block doesn't extend into next block's time

#### 4. Block
**Location**: `framework/info/Block.java`

**Responsibilities**:
- Represents a single scheduled event
- Stores event metadata

**Properties**:
- `Date date`: Start time
- `long length`: Duration in milliseconds
- `BlockType type`: Event category
- `boolean reoccurring`: Weekly recurrence flag
- `Color color`: Visual representation color
- `String name`: User-defined label

**Defaults**:
- Type: `BlockType.NAP`
- Reoccurring: `false`
- Color: RGB(0, 200, 255)
- Name: "Nap"

#### 5. BlockType (Enumeration)
**Location**: `framework/info/BlockType.java`

Defines event categories:
- `CLASS`: Academic classes
- `WORK`: Work shifts
- `EVENT`: General events
- `SLEEP`: Main sleep periods
- `NAP`: Supplementary rest
- `FREE`: Unused (legacy)

#### 6. GraphicSchedule
**Location**: `framework/info/gui/GraphicSchedule.java`

**Responsibilities**:
- Main application window
- User interaction handling
- Workflow orchestration (first-time setup, tutorial)

**UI Components**:
- `GridPanel`: Weekly schedule grid
- `WakeTimePanel`: Wake time preference editor
- `AboutPanel`: Application information
- `StartPanel`: Welcome screen
- Various dialog windows

**Event Handlers**:
- `GridMouseListener`: Grid interactions (double-click to edit)
- `GridButtonListener`: Side button actions (Sleep, Wake, Stats, etc.)
- `OptionButtonListener`: Options menu actions
- `TutorialButtonListener`: Tutorial navigation

#### 7. GridPanel
**Location**: `framework/info/gui/GridPanel.java`

**Responsibilities**:
- Renders the weekly schedule grid
- Displays time labels and day headers
- Manages GraphicBlock positioning

**Layout**:
- 7 columns (Monday-Sunday)
- 48 rows (30-minute intervals, 24 hours)
- Scrollable viewport (1211px height)

#### 8. GraphicBlock
**Location**: `framework/info/gui/GraphicBlock.java`

**Responsibilities**:
- Visual representation of a Block
- Renders event name, time, delete button
- Handles mouse interactions

**Visual Properties**:
- Color: Inherited from Block
- Position: Calculated from date and day
- Height: Proportional to duration

#### 9. ScheduleFileIO
**Location**: `framework/info/io/ScheduleFileIO.java`

**Responsibilities**:
- Saves/loads schedule data to/from file
- Manages application state persistence
- Error logging

**File Operations**:
- `saveSchedule()`: Complete save (all data)
- `loadSchedule()`: Complete load
- `saveBlocks()`: Save only blocks
- `savePreference()`: Save nap preference
- `saveStats()`: Save statistics

**Error Handling**:
- Logs errors to separate log file
- Returns default values on read failures
- Creates new file if missing

## Data Flow

### Application Startup Flow

```
1. GraphicSchedule.main()
   ├─→ 2. GraphicSchedule.initialize()
   │      ├─→ Create JFrame window
   │      ├─→ Create GridPanel
   │      ├─→ Create buttons
   │      └─→ Initialize listeners
   │
   ├─→ 3. GraphicSchedule.run()
   │      ├─→ SchedulePlanner.load()
   │      │      └─→ ScheduleFileIO.loadSchedule()
   │      │
   │      └─→ If first time:
   │             ├─→ doStartscreen()
   │             ├─→ editSleepPreferences()
   │             └─→ showTutorial()
   │
   └─→ 4. drawSchedule()
          └─→ Render all blocks on GridPanel
```

### Event Creation Flow

```
1. User double-clicks grid
   │
   ├─→ 2. GridMouseListener.mouseClicked()
   │      └─→ drawBlockEditMenu(DEFAULT_BLOCK)
   │
   ├─→ 3. User fills form and clicks "Done"
   │      │
   │      ├─→ 4. Create Block object(s) for selected days
   │      │
   │      ├─→ 5. SchedulePlanner.addEvent(block)
   │      │      └─→ Schedule.addBlock(block)
   │      │             ├─→ Binary search insertion point
   │      │             ├─→ Check for overlaps
   │      │             └─→ Insert if valid
   │      │
   │      ├─→ 6. Create GraphicBlock
   │      │      └─→ Add to GridPanel
   │      │
   │      ├─→ 7. schedule.saveBlocks()
   │      │      └─→ ScheduleFileIO.saveAllBlocks()
   │      │
   │      └─→ 8. schedule.applyAlgorithm()
   │             └─→ SleepAlgorithm.applyAlgorithm()
```

### Sleep Algorithm Flow

```
1. SchedulePlanner.applyAlgorithm()
   │
   ├─→ 2. SleepAlgorithm.applyAlgorithm(schedule, today)
   │      │
   │      ├─→ 3. stats(schedule, today)
   │      │      └─→ Calculate sleep statistics
   │      │
   │      ├─→ 4. If nap preference enabled:
   │      │      ├─→ addNaps(schedule, today)
   │      │      │      ├─→ For each day:
   │      │      │      │      └─→ organizeNapInDay()
   │      │      │      │             ├─→ Find gaps between events
   │      │      │      │             ├─→ Schedule naps in gaps
   │      │      │      │             └─→ Check overlap
   │      │      │      │
   │      │      └─→ addLongerSleep(schedule, today)
   │      │             ├─→ For each sleep block:
   │      │             │      ├─→ Calculate missing sleep
   │      │             │      ├─→ Extend block start time
   │      │             │      └─→ checkOverlap()
   │      │             │
   │      │             └─→ Return
   │      │
   │      └─→ 5. Else:
   │             └─→ addLongerSleep(schedule, today)
   │
   └─→ 6. GUI updates to reflect changes
```

### Sleep Tracking Flow

```
1. User clicks "Sleep" button
   │
   ├─→ 2. GridButtonListener.actionPerformed()
   │      │
   │      ├─→ 3. swapSleepButton()
   │      │      ├─→ Disable "Sleep" button
   │      │      └─→ Enable "Wake" button
   │      │
   │      ├─→ 4. schedule.goToSleep()
   │      │      ├─→ Create Date(now)
   │      │      ├─→ schedule.setPreviousSleep(date)
   │      │      └─→ ScheduleFileIO.saveSleep(date)
   │      │
   │      ├─→ 5. SleepAlgorithm.setProgramExitTimestamp(now)
   │      │
   │      └─→ 6. schedule.save()
   │
   └─→ 7. User clicks "Wake" button
          │
          ├─→ 8. swapSleepButton()
          │      ├─→ Enable "Sleep" button
          │      └─→ Disable "Wake" button
          │
          └─→ 9. schedule.wakeUp()
                 ├─→ Reset sleep timestamp
                 └─→ ScheduleFileIO.saveSleep(Date(0))
```

## Algorithm Design

### Binary Insertion Algorithm

The Schedule class uses binary search to maintain blocks in sorted order:

```java
public int findLocationFor(Block b) {
    // Returns the index where block should be inserted
    // Uses binary search on block start times
    // O(log n) complexity
}

public boolean canInsertAt(Block b, int location) {
    // Checks if insertion would cause overlap
    // Compares with previous and next blocks
    // O(1) complexity
}
```

**Advantages**:
- Fast search: O(log n)
- Maintains sorted order
- Simple overlap detection

**Tradeoffs**:
- Insertion is O(n) due to ArrayList shifting
- Acceptable because:
  - Schedule modifications are infrequent
  - Block searches are frequent
  - N is typically small (< 100 blocks)

### Sleep Optimization Algorithm

#### Strategy Selection

```
IF user prefers naps:
    1. Add strategic naps (up to 1 hour/day)
    2. Extend sleep blocks if still needed
ELSE:
    1. Extend sleep blocks only
```

#### Extension Algorithm

The extension algorithm works backward from required sleep:

```
Required Sleep: 56 hours/week
Actual Sleep: Sum of all SLEEP and NAP blocks

For each sleep block from current day forward:
    Missing = Required - Actual
    
    IF Missing >= 2 hours:
        Extend by 2 hours
    ELSE IF Missing > 0:
        Extend by Missing
    ELSE:
        Break (requirement met)
    
    Adjust for overlaps
    Recalculate Missing
```

#### Nap Placement Algorithm

```
For each day starting from current:
    Get all blocks between wake-up and sleep
    
    For each gap between consecutive blocks:
        IF gap >= 1 hour + 30 min buffers:
            Schedule 1-hour nap
        ELSE IF gap in [45 min, 1 hour]:
            Schedule (gap - 30 min) nap
        ELSE:
            Skip (too small)
        
        Add 15-min buffers before and after
        
    IF total naps for day >= 1 hour:
        Continue to next day
```

**Special Cases**:
- No nap immediately after wake-up (3-hour buffer)
- No nap immediately before sleep block
- Skip past days (can't schedule in the past)

### Overlap Detection and Resolution

```java
private static void checkOverlap(Block test, Schedule schedule) {
    int index = schedule.findLocationFor(test);
    Block previous = schedule.get(index - 1);
    Block next = schedule.get(index);
    
    // Check overlap with previous block
    if (test.start < previous.end) {
        timeOverlap = previous.end - test.start;
        test.start += timeOverlap + 15 minutes;
        test.length -= timeOverlap + 15 minutes;
    }
    
    // Check overlap with next block
    if (test.end > next.start) {
        timeOverlap = test.end - next.start;
        test.start -= timeOverlap + 15 minutes;
        test.length -= timeOverlap + 15 minutes;
    }
}
```

### Block Spanning Two Days

When a block crosses midnight, it's split:

```java
if (doesBlockSpanTwoDays(block)) {
    Block firstHalf = getFirstHalfBlockOfBlock(block);  // Before midnight
    Block lastHalf = getLastHalfBlockOfBlock(block);    // After midnight
    schedule.add(firstHalf);
    schedule.add(lastHalf);
} else {
    schedule.add(block);
}
```

**Calculation**:
```
Midnight = (block.date + 1 day) set to 00:00:00

First Half Length = Midnight - block.start
Last Half Length = block.length - First Half Length

First Half: [block.start, Midnight)
Last Half: [Midnight, Midnight + Last Half Length)
```

## File Format Specification

### Binary File Structure

```
version=<version number>
$<Sleep timestamp (long, 8 bytes)>
E<Exit timestamp (long, 8 bytes)>
P<Nap preference (boolean, 1 byte)>
S<Stat 0 (long, 8 bytes)><Stat 1 (long)>...<Stat N (long)>
W<Monday wake time (long)><Tuesday wake time (long)>...<Sunday wake time (long)>
#<Block type (byte)><Block date (long)><Block length (long)><Reoccurring (boolean)>
#<Next block...>
...
```

### Field Descriptions

- **Version**: Text string indicating file format version
- **Sleep Timestamp ($)**: Last time user clicked "Sleep"
- **Exit Timestamp (E)**: Last time application closed
- **Nap Preference (P)**: true if user wants naps
- **Stats (S)**: Array of 5 long values for sleep statistics
- **Wake Times (W)**: 7 long values (Monday-Sunday)
- **Blocks (#)**: Variable number of block entries

### Block Encoding

Each block consists of:
1. **Type** (1 byte): BlockType ordinal
2. **Date** (8 bytes): Start time in milliseconds since epoch
3. **Length** (8 bytes): Duration in milliseconds
4. **Reoccurring** (1 byte): 0x01 for true, 0x00 for false
5. **Color** (12 bytes): RGB values (3 integers)
6. **Name** (variable): UTF-8 string

### Example

```
version=1.0
$1699564800000
E1699651200000
P01
S0000000000000000000000000001C20000000000...
W00000001699401600000...
#020000000169956480000000000000001C20000001FF000000...
```

## UML Diagrams

### Class Diagram Overview

The main relationships:

```
GraphicSchedule --> SchedulePlanner
SchedulePlanner --> Schedule
SchedulePlanner --> SleepAlgorithm
SchedulePlanner --> ScheduleFileIO
Schedule --> Block
Block --> BlockType
GridPanel --> GraphicBlock
GraphicBlock --> Block
```

### Component Dependencies

```
┌─────────────────┐
│ GraphicSchedule │
└────────┬────────┘
         │
    ┌────▼──────┐
    │SchedulePlanner│
    └────┬──────┘
         │
    ┌────▼────┐
    │Schedule │◄──────────┐
    └────┬────┘           │
         │                │
    ┌────▼────┐      ┌────┴────────┐
    │  Block  │      │SleepAlgorithm│
    └────┬────┘      └─────────────┘
         │
    ┌────▼────┐
    │BlockType│
    └─────────┘
```

### Complete UML

For the complete UML diagram, see `src/main_uml.txt`. This file contains PlantUML code that can be rendered using the PlantUML tool or online renderers like:
- https://www.plantuml.com/plantuml/
- https://plantuml-editor.kkeisuke.dev/

## Design Decisions

### Why Static Classes?

`SleepAlgorithm` and `ScheduleFileIO` are designed as static utility classes because:

1. **Single Responsibility**: They perform pure functions without instance state
2. **Global Access**: Needed throughout the application
3. **Performance**: No object creation overhead
4. **Simplicity**: Clear that only one instance is needed

### Why ArrayList Over Array?

The Schedule uses `ArrayList<Block>` instead of fixed arrays because:

1. **Dynamic Size**: Number of events is unknown and variable
2. **Type Safety**: Generic type provides compile-time type checking
3. **Convenience**: Built-in methods (add, remove, clear)
4. **Binary Search**: Collections.binarySearch available

### Why Binary Format?

The custom binary file format was chosen over serialization or text formats because:

1. **Efficiency**: Smaller file size, faster I/O
2. **Control**: Precise field ordering and versioning
3. **Learning**: Educational value in implementing custom format
4. **Portability**: Not dependent on Java serialization version

### Why Swing Over JavaFX?

Java Swing was used because:

1. **Availability**: Included in Java 8 without additional modules
2. **Maturity**: Well-documented, stable API
3. **Simplicity**: Easier learning curve for the team
4. **Compatibility**: Works across all Java 8+ installations

## Performance Considerations

### Time Complexities

- **Block Insertion**: O(n) - ArrayList shift
- **Block Search**: O(log n) - Binary search
- **Block Removal**: O(n) - ArrayList shift
- **Get All Blocks**: O(n) - Iterate list
- **Algorithm Execution**: O(n²) - Nested loops in nap placement

### Space Complexities

- **Schedule Storage**: O(n) - Linear in number of blocks
- **Algorithm**: O(n) - Temporary arrays for filtered blocks
- **GUI**: O(n) - GraphicBlock for each Block

### Optimization Opportunities

1. **Caching**: Could cache filtered block lists (by type, by day)
2. **Lazy Updates**: Defer algorithm execution until explicit trigger
3. **Incremental Updates**: Update only affected sleep blocks
4. **Data Structure**: Consider TreeSet for O(log n) insertion

## Testing Strategy

### Manual Testing Approach

The application was primarily tested through:

1. **User Scenarios**: Complete workflows (first-time setup, event creation, sleep tracking)
2. **Edge Cases**: Overlapping events, midnight-spanning blocks, past days
3. **Algorithm Validation**: Verify sleep extension and nap placement logic
4. **File I/O**: Save/load cycles, error handling
5. **GUI Interaction**: All buttons, dialogs, and mouse interactions

### Test Methods in Code

Several classes include `main()` methods for unit testing:

- `SchedulePlanner.main()`: Tests schedule operations
- `SleepAlgorithm.main()`: Tests algorithm logic
- `Schedule.main()`: Tests block management

These were used during development for targeted testing of individual components.

## Future Architecture Improvements

### Recommended Enhancements

1. **Dependency Injection**: Replace static classes with injected instances
2. **Observer Pattern**: Event system for model changes
3. **Command Pattern**: Undo/redo functionality
4. **Repository Pattern**: Abstract data access layer
5. **DTO Pattern**: Separate data transfer objects from domain models
6. **Unit Tests**: Comprehensive JUnit test suite
7. **Logging Framework**: Replace custom logging with SLF4J or Log4j

### Modernization Path

To modernize the codebase:

1. **JavaFX Migration**: Modern GUI framework
2. **Java 17+**: Records, pattern matching, sealed classes
3. **Build System**: Maven or Gradle for dependency management
4. **Database**: SQLite or H2 for relational data
5. **JSON**: Modern data format for interoperability
6. **REST API**: Web service layer for cloud sync

---

*This architecture document reflects the design decisions, patterns, and implementation details of Noct-Journal as of the latest version. For implementation details, refer to the source code comments and embedded PlantUML documentation.*
