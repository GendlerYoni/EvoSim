# EvoSim Frontend

The frontend for **EvoSim**, an interactive 2D evolution and natural selection simulator.

Built with React and TypeScript, it provides the user interface for configuring and interacting with the simulation.

## Tech Stack

- React 19
- TypeScript
- Vite
- React Router
- Vitest
- React Testing Library

## Getting Started

### Prerequisites

- Node.js
- npm

### Installation

From the `frontend/` directory:

```bash
npm install
```

### Development

Start the local development server:

```bash
npm run dev
```

Open the local URL printed by Vite (typically `http://localhost:5173`).

The root URL redirects to `/configuration`.

## Current Features

- Simulation configuration form
- Configurable initial herbivore and predator populations
- Initial food quantity and maximum food spawn per tick
- Numeric inputs, sliders, and increment/decrement controls
- Press-and-hold controls
- Input validation and error messages
- Reset to default values
- Configuration payload generation

**Current milestone:** M14 — React Configuration Foundation.

The configuration form currently outputs its validated configuration to the browser console. Backend integration will be implemented in a later milestone.

## Testing and Quality Checks

Run automated tests:

```bash
npm run test
```

Run the linter:

```bash
npm run lint
```

Create a production build:

```bash
npm run build
```

## Project Structure

```text
src/
├── features/
│   └── configuration/
├── tests/
│   ├── configuration/
│   └── setup.ts
├── App.tsx
├── index.css
└── main.tsx
```

The simulation engine is implemented separately in the Java backend. The frontend handles presentation and user interaction, while the engine remains responsible for simulation rules.

For the overall project architecture and development roadmap, see the [root README](../README.md).