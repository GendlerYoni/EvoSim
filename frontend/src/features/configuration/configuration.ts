export type SimulationConfiguration = {
    initialHerbivores: number
    initialPredators: number
    initialFood: number
    foodSpawnMaxPerTick: number
}

export type ConfigurationForm = {
    initialHerbivores: string
    initialPredators: string
    initialFood: string
    foodSpawnMaxPerTick: string
}

export const DEFAULT_FORM_VALUES: ConfigurationForm = {
    initialHerbivores: "100",
    initialPredators: "100",
    initialFood: "100",
    foodSpawnMaxPerTick: "5"
}

export const CONFIG_LIMITS = {
    initialHerbivores: { min: 0, max: 1000 },
    initialPredators: { min: 0, max: 1000 },
    initialFood: { min: 0, max: 5000 },
    foodSpawnMaxPerTick: { min: 0, max: 100 }
}