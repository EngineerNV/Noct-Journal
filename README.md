# Noct-Journal: Intelligent Sleep Schedule Management System

[![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-Educational-green.svg)]()
[![GUI](https://img.shields.io/badge/GUI-Java%20Swing-orange.svg)]()

## 📋 Project Overview

**Noct-Journal** is a sophisticated desktop application designed to help users optimize their sleep schedules while managing daily commitments. The application intelligently adjusts sleep times to ensure users receive adequate rest (56 hours per week recommended) without interfering with their work, classes, and other scheduled events.

### 🎯 Project Objectives

The primary goal of Noct-Journal is to solve the common problem of inadequate sleep among busy individuals by:

1. **Automating Sleep Scheduling**: Intelligently calculating optimal sleep times based on user events and preferences
2. **Tracking Sleep Patterns**: Monitoring actual sleep duration versus required sleep hours
3. **Flexible Event Management**: Supporting various event types (work, classes, personal events) with recurring scheduling
4. **Nap Optimization**: Strategically scheduling naps when users prefer them to supplement sleep
5. **Visual Schedule Management**: Providing an intuitive weekly calendar view for easy schedule visualization

## ✨ Key Features

### Core Functionality

- **Visual Weekly Schedule Grid**: Interactive 7-day calendar view with time-based blocks
- **Intelligent Sleep Algorithm**: Automatically adjusts sleep times to meet 56-hour weekly sleep requirement
- **Sleep Tracking**: Real-time tracking of sleep sessions via "Sleep" and "Wake" buttons
- **Event Management**:
  - Create, edit, and delete events
  - Support for multiple event types (Class, Work, Event, Sleep, Nap)
  - Recurring event support for weekly schedules
  - Lock/unlock events to carry them into the next week
  - Custom color coding for different event types
- **Nap Scheduling**: Automatic nap placement when enabled, with smart conflict avoidance
- **Sleep Statistics**: Track hours slept, hours needed, and sleep deficit
- **Data Persistence**: Automatic save/load of schedules and preferences

### User Interface Features

- **First-Time Setup Wizard**: Interactive tutorial for new users
- **Wake Time Preferences**: Configurable wake-up times for each day of the week
- **Block Editor**: Full-featured editor for creating and modifying schedule events
- **Statistics Dashboard**: Visual representation of sleep metrics
- **About Screen**: Application information and credits

## 🛠️ Technology Stack

### ✨ Modern UI refresh

The front end now uses a **modernized Nimbus Swing theme** (no third-party UI dependency) with:
- Rounded controls and cards
- Improved dark surfaces for side panels
- Softer calendar grid colors and typography
- Smoother event block visuals with rounded corners


### Programming Language & Framework
- **Java 17 LTS**: Core programming language runtime
- **Java Swing**: GUI framework for desktop interface
- **Java AWT**: Graphics and event handling

### Key Libraries & APIs
- `java.util.GregorianCalendar`: Date and time management
- `java.util.ArrayList`: Dynamic schedule data structures
- `java.io.RandomAccessFile`: Custom binary file I/O for data persistence
- `javax.swing.*`: Complete GUI component library
- `java.awt.Color`: Color management for visual differentiation

### Development Tools
- **PlantUML**: UML diagram generation for architecture documentation
- **Git**: Version control

### Design Patterns
- **Singleton Pattern**: Static utility classes (SleepAlgorithm, ScheduleFileIO)
- **Model-View Architecture**: Separation of data (Schedule, Block) from presentation (GUI components)
- **Observer Pattern**: Event listeners for GUI interactions
- **Strategy Pattern**: Different sleep optimization strategies (naps vs. longer sleep)

## 📐 Architecture

### System Architecture

The application follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (GraphicSchedule, GUI Panels)          │
├─────────────────────────────────────────┤
│         Business Logic Layer            │
│  (SchedulePlanner, SleepAlgorithm)      │
├─────────────────────────────────────────┤
│         Data Layer                      │
│  (Schedule, Block, BlockType)           │
├─────────────────────────────────────────┤
│         Persistence Layer               │
│  (ScheduleFileIO, ByteConverter)        │
└─────────────────────────────────────────┘
```

### Component Structure

#### Core Components

1. **SchedulePlanner** (`framework/SchedulePlanner.java`)
   - Central coordinator for schedule operations
   - Manages calendar operations and event manipulation
   - Interfaces with I/O and algorithm subsystems

2. **SleepAlgorithm** (`framework/info/SleepAlgorithm.java`)
   - Implements intelligent sleep scheduling algorithms
   - Handles nap placement and sleep extension
   - Tracks sleep statistics and metrics
   - Manages wake-up time preferences

3. **Schedule** (`framework/info/grid/Schedule.java`)
   - Maintains the ordered list of schedule blocks
   - Provides block insertion, removal, and retrieval operations
   - Implements binary insertion for efficient sorted storage

4. **Block** (`framework/info/Block.java`)
   - Represents individual schedule events
   - Contains date, length, type, recurrence, color, and name properties
   - Supports multiple block types (Sleep, Nap, Class, Work, Event)

5. **GraphicSchedule** (`framework/info/gui/GraphicSchedule.java`)
   - Main application window and entry point
   - Manages all GUI interactions and user workflows
   - Coordinates between UI components and business logic

6. **ScheduleFileIO** (`framework/info/io/ScheduleFileIO.java`)
   - Handles file-based data persistence
   - Custom binary format for efficient storage
   - Saves/loads schedules, preferences, and statistics

For detailed architecture information, see [ARCHITECTURE.md](ARCHITECTURE.md).

## 🚀 Installation & Usage

### System Requirements

- **Operating System**: Windows, macOS, or Linux
- **Java Runtime Environment**: Java 17 or higher
- **Minimum Screen Resolution**: 1280x720
- **Recommended RAM**: 512 MB

### Installation Steps

> Note: Maven builds may be blocked in restricted networks (e.g., HTTP 403 to Maven Central). The `javac` path above is the primary supported workflow.

1. **Clone the repository**:
   ```bash
   git clone https://github.com/EngineerNV/Noct-Journal.git
   cd Noct-Journal
   ```

2. **Compile the source code**:
   ```bash
   cd src
   javac framework/info/gui/GraphicSchedule.java
   ```

3. **Run the application**:
   ```bash
   java framework.info.gui.GraphicSchedule
   ```

   To skip the tutorial on first run:
   ```bash
   java framework.info.gui.GraphicSchedule -d
   ```

### Quick Start Guide

1. **First Launch**: Set your preferred wake-up times for each day of the week
2. **Choose Nap Preference**: Indicate whether you want the system to schedule naps
3. **Complete Tutorial**: Follow the interactive tutorial to learn key features
4. **Add Events**: Double-click the grid to create events (classes, work, etc.)
5. **Track Sleep**: Click "Sleep" before bed and "Wake" when you wake up
6. **Review Stats**: Check your sleep statistics to monitor your sleep health

### Creating Events

- **Double-click** empty grid space to create a new event
- **Double-click** an existing event to edit it
- **Click the 'X'** on an event to delete it
- **Lock icon** makes events recurring (carry to next week)
- Choose event type, time, days, and color

## 📊 Sleep Algorithm Details

The application uses two complementary strategies to ensure adequate sleep:

### 1. Extended Sleep Duration
- Identifies existing sleep blocks
- Extends them by up to 2 hours per day
- Avoids overlaps with existing events
- Prioritizes current and future days

### 2. Strategic Nap Placement (when enabled)
- Finds gaps between events during the day
- Schedules naps of 15 minutes to 1 hour
- Maintains 15-minute buffers before/after events
- Avoids scheduling immediately after wake-up (3-hour buffer)
- Maximum 1 hour of naps per day

Both strategies work together to reach the 56-hour weekly sleep target while respecting the user's schedule constraints.

## 📁 Project Structure

```
Noct-Journal/
├── src/
│   ├── framework/
│   │   ├── SchedulePlanner.java          # Main schedule coordinator
│   │   ├── info/
│   │   │   ├── Block.java                # Schedule event representation
│   │   │   ├── BlockType.java            # Event type enumeration
│   │   │   ├── SleepAlgorithm.java       # Sleep optimization logic
│   │   │   ├── grid/
│   │   │   │   └── Schedule.java         # Schedule data structure
│   │   │   ├── gui/
│   │   │   │   ├── GraphicSchedule.java  # Main application window
│   │   │   │   ├── GridPanel.java        # Schedule grid display
│   │   │   │   ├── GraphicBlock.java     # Visual block representation
│   │   │   │   ├── AboutPanel.java       # About screen
│   │   │   │   ├── StartPanel.java       # Welcome screen
│   │   │   │   ├── WakeTimePanel.java    # Wake time preferences
│   │   │   │   └── GraphPanel.java       # Statistics graph
│   │   │   └── io/
│   │   │       └── ScheduleFileIO.java   # File persistence
│   │   └── util/
│   │       └── ByteConverter.java        # Binary data conversion
│   └── main_uml.txt                      # PlantUML architecture diagram
├── README.md                             # This file
└── ARCHITECTURE.md                       # Detailed architecture documentation
```

## 👥 Development Team

- **Andre Allan Ponce**: Core architecture, SchedulePlanner, SleepAlgorithm, Block system, File I/O
- **Nick Legend**: Sleep algorithm implementation, nap scheduling logic
- **Victor Zamarian**: GUI framework, GraphicSchedule interface
- **Lonny Raspberry**: User interface components and interactions

## 📝 Development Notes

### Code Documentation
All classes include embedded PlantUML code for generating UML diagrams. When making modifications to the code, please update the corresponding PlantUML comments to maintain accurate documentation.

### UML Diagram Generation
The project includes PlantUML definitions within source files. To generate diagrams:
1. Extract PlantUML code from comments in `.java` files
2. Use PlantUML tool or online editor to render diagrams
3. Main architecture diagram is defined in `src/main_uml.txt`

## 🔮 Future Enhancements

Potential areas for future development:

- Migration to modern Java versions (Java 11+)
- Database integration for better data persistence
- Cloud sync capabilities
- Mobile companion app
- Advanced analytics and sleep quality metrics
- Integration with wearable devices
- Export to popular calendar formats (iCal, Google Calendar)
- Improved graph visualization for statistics

## 📜 License

This project was created for educational purposes as part of a software engineering course.

## 🙏 Acknowledgments

Special thanks to the instructors and peers who provided guidance and feedback during the development of this project.

---

**Note**: This is a portfolio project demonstrating software engineering principles, Java Swing GUI development, algorithm design, and data persistence techniques.