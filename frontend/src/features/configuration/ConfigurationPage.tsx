import './ConfigurationPage.css'

import { Fragment, useState, type FormEvent } from 'react'

import {
    CONFIG_LIMITS,
    DEFAULT_FORM_VALUES,
    type ConfigurationForm,
    type SimulationConfiguration
} from './configuration'

import NumberSetting from './NumberSetting'

function ConfigurationPage() {
    const [config, setConfig] = useState<ConfigurationForm>(DEFAULT_FORM_VALUES)
    const [resetVersion, setResetVersion] = useState(0)
    const [showValidationErrors, setShowValidationErrors] = useState(false)

    const validationErrors = {
        initialHerbivores: getSettingError(
            config.initialHerbivores,
            CONFIG_LIMITS.initialHerbivores
        ),
        initialPredators: getSettingError(
            config.initialPredators,
            CONFIG_LIMITS.initialPredators
        ),
        initialFood: getSettingError(
            config.initialFood,
            CONFIG_LIMITS.initialFood
        ),
        foodSpawnMaxPerTick: getSettingError(
            config.foodSpawnMaxPerTick,
            CONFIG_LIMITS.foodSpawnMaxPerTick
        )
    }

    function handleReset() {
        setConfig(DEFAULT_FORM_VALUES)
        setResetVersion(prev => prev + 1)
        setShowValidationErrors(false)
    }

    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        const hasErrors = Object.values(validationErrors).some(
            error => error !== undefined
        )

        if (hasErrors) {
            setShowValidationErrors(true)
            return
        }

        setShowValidationErrors(false)

        const simulationConfig: SimulationConfiguration = {
            initialHerbivores: Number(config.initialHerbivores),
            initialPredators: Number(config.initialPredators),
            initialFood: Number(config.initialFood),
            foodSpawnMaxPerTick: Number(config.foodSpawnMaxPerTick)
        }

        console.log(simulationConfig)
    }

    return (
        <main className="configuration-page">
            <h1>Simulation Configuration</h1>

            <form onSubmit={handleSubmit} noValidate>
                <Fragment key={resetVersion}>
                    <NumberSetting
                        label="Initial Herbivores"
                        value={config.initialHerbivores}
                        min={CONFIG_LIMITS.initialHerbivores.min}
                        max={CONFIG_LIMITS.initialHerbivores.max}
                        onChange={(newValue) => {
                            setConfig(prev => ({
                                ...prev,
                                initialHerbivores: newValue
                            }))
                        }}
                        error={
                            showValidationErrors
                                ? validationErrors.initialHerbivores
                                : undefined
                        }
                    />

                    <NumberSetting
                        label="Initial Predators"
                        value={config.initialPredators}
                        min={CONFIG_LIMITS.initialPredators.min}
                        max={CONFIG_LIMITS.initialPredators.max}
                        onChange={(newValue) => {
                            setConfig(prev => ({
                                ...prev,
                                initialPredators: newValue
                            }))
                        }}
                        error={
                            showValidationErrors
                                ? validationErrors.initialPredators
                                : undefined
                        }
                    />

                    <section>
                        <h2>Food Settings</h2>

                        <NumberSetting
                            label="Initial Food"
                            value={config.initialFood}
                            min={CONFIG_LIMITS.initialFood.min}
                            max={CONFIG_LIMITS.initialFood.max}
                            onChange={(newValue) => {
                                setConfig(prev => ({
                                    ...prev,
                                    initialFood: newValue
                                }))
                            }}
                            error={
                                showValidationErrors
                                    ? validationErrors.initialFood
                                    : undefined
                            }
                        />

                        <NumberSetting
                            label="Max Food Spawn per Tick"
                            value={config.foodSpawnMaxPerTick}
                            min={CONFIG_LIMITS.foodSpawnMaxPerTick.min}
                            max={CONFIG_LIMITS.foodSpawnMaxPerTick.max}
                            onChange={(newValue) => {
                                setConfig(prev => ({
                                    ...prev,
                                    foodSpawnMaxPerTick: newValue
                                }))
                            }}
                            error={
                                showValidationErrors
                                    ? validationErrors.foodSpawnMaxPerTick
                                    : undefined
                            }
                        />
                    </section>
                </Fragment>

                <button type="button" onClick={handleReset}>
                    Reset to Defaults
                </button>

                <button type="submit">
                    Start Simulation
                </button>
            </form>
        </main>
    )
}

function getSettingError(
    value: string,
    limits: { min: number; max: number }
): string | undefined {
    if (value.trim() === '') {
        return 'This field is required'
    }

    const number = Number(value)

    if (
        !/^\d+$/.test(value) ||
        !Number.isSafeInteger(number) ||
        number < limits.min ||
        number > limits.max
    ) {
        return `Enter a whole number between ${limits.min} and ${limits.max}`
    }

    return undefined
}

export default ConfigurationPage
