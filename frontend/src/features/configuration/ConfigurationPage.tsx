
import './ConfigurationPage.css'

import { Fragment, useEffect, useRef, useState, type FormEvent } from 'react'

import { createSimulation, SimulationApiError } from '../../api/simulations'
import type { CreateSimulationRequest } from '../../api/simulationTypes'

import {
    CONFIG_LIMITS,
    DEFAULT_FORM_VALUES,
    type ConfigurationForm,
} from './configuration'

import NumberSetting from './NumberSetting'

function ConfigurationPage() {
    const [config, setConfig] = useState<ConfigurationForm>(DEFAULT_FORM_VALUES)
    const [resetVersion, setResetVersion] = useState(0)
    const [showValidationErrors, setShowValidationErrors] = useState(false)
    const [isLoading, setIsLoading] = useState(false)
    const [requestError, setRequestError] = useState<string | null>(null)
    const [serverFieldErrors, setServerFieldErrors] =
        useState<Record<string, string>>({})

    const isSubmittingRef = useRef(false)
    const isMountedRef = useRef(false)

    useEffect(() => {
        isMountedRef.current = true

        return () => {
            isMountedRef.current = false
        }
    }, [])

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
        setRequestError(null)
        setServerFieldErrors({})
    }

    function updateSetting(field: keyof ConfigurationForm, value: string) {
        setConfig(prev => ({
            ...prev,
            [field]: value
        }))

        setRequestError(null)

        setServerFieldErrors(prev => {
            const updated = { ...prev }
            delete updated[field]
            return updated
        })
    }

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()

        // Prevent concurrent submissions.
        if (isSubmittingRef.current) {
            return
        }

        setRequestError(null)
        setServerFieldErrors({})

        const hasErrors = Object.values(validationErrors).some(
            error => error !== undefined
        )

        if (hasErrors) {
            setShowValidationErrors(true)
            return
        }

        setShowValidationErrors(false)

        const simulationConfig: CreateSimulationRequest = {
            initialHerbivores: Number(config.initialHerbivores),
            initialPredators: Number(config.initialPredators),
            initialFood: Number(config.initialFood),
            foodSpawnMaxPerTick: Number(config.foodSpawnMaxPerTick)
        }

        isSubmittingRef.current = true
        setIsLoading(true)

        try {
            const result = await createSimulation(simulationConfig)

            // Ignore responses after leaving the page.
            if (!isMountedRef.current) {
                return
            }

            sessionStorage.setItem('sessionId', result.sessionId)

            // Temporary until navigation is implemented in M17.
            console.log('Simulation created:', result.sessionId)

        } catch (error) {
            if (!isMountedRef.current) {
                return
            }

            if (error instanceof SimulationApiError) {
                setServerFieldErrors(error.fieldErrors)

                if (Object.keys(error.fieldErrors).length > 0) {
                    setRequestError(null)
                } else if (error.code === 'NETWORK_ERROR') {
                    setRequestError(
                        'Unable to connect to the server. Please try again.'
                    )
                } else if (error.code === 'SERVER_AT_CAPACITY') {
                    setRequestError(
                        'The server is at capacity. Please try again later.'
                    )
                } else {
                    setRequestError(error.message)
                }
            } else {
                setRequestError(
                    'An unexpected error occurred. Please try again.'
                )
            }
        } finally {
            isSubmittingRef.current = false

            if (isMountedRef.current) {
                setIsLoading(false)
            }
        }
    }

    return (
        <main className="configuration-page">
            <h1>Simulation Configuration</h1>

            <form onSubmit={handleSubmit} noValidate>
                <fieldset
                    className="configuration-fieldset"
                    disabled={isLoading}
                >
                    <Fragment key={resetVersion}>
                        <NumberSetting
                            label="Initial Herbivores"
                            value={config.initialHerbivores}
                            min={CONFIG_LIMITS.initialHerbivores.min}
                            max={CONFIG_LIMITS.initialHerbivores.max}
                            disabled={isLoading}
                            onChange={(newValue) =>
                                updateSetting('initialHerbivores', newValue)
                            }
                            error={
                                (showValidationErrors
                                    ? validationErrors.initialHerbivores
                                    : undefined)
                                ?? serverFieldErrors.initialHerbivores
                            }
                        />

                        <NumberSetting
                            label="Initial Predators"
                            value={config.initialPredators}
                            min={CONFIG_LIMITS.initialPredators.min}
                            max={CONFIG_LIMITS.initialPredators.max}
                            disabled={isLoading}
                            onChange={(newValue) =>
                                updateSetting('initialPredators', newValue)
                            }
                            error={
                                (showValidationErrors
                                    ? validationErrors.initialPredators
                                    : undefined)
                                ?? serverFieldErrors.initialPredators
                            }
                        />

                        <section>
                            <h2>Food Settings</h2>

                            <NumberSetting
                                label="Initial Food"
                                value={config.initialFood}
                                min={CONFIG_LIMITS.initialFood.min}
                                max={CONFIG_LIMITS.initialFood.max}
                                disabled={isLoading}
                                onChange={(newValue) =>
                                    updateSetting('initialFood', newValue)
                                }
                                error={
                                    (showValidationErrors
                                        ? validationErrors.initialFood
                                        : undefined)
                                    ?? serverFieldErrors.initialFood
                                }
                            />

                            <NumberSetting
                                label="Max Food Spawn per Tick"
                                value={config.foodSpawnMaxPerTick}
                                min={CONFIG_LIMITS.foodSpawnMaxPerTick.min}
                                max={CONFIG_LIMITS.foodSpawnMaxPerTick.max}
                                disabled={isLoading}
                                onChange={(newValue) =>
                                    updateSetting('foodSpawnMaxPerTick', newValue)
                                }
                                error={
                                    (showValidationErrors
                                        ? validationErrors.foodSpawnMaxPerTick
                                        : undefined)
                                    ?? serverFieldErrors.foodSpawnMaxPerTick
                                }
                            />
                        </section>
                    </Fragment>

                    {requestError && (
                        <p className="configuration-request-error" role="alert">
                            {requestError}
                        </p>
                    )}

                    <button type="button" onClick={handleReset}>
                        Reset to Defaults
                    </button>

                    <button type="submit">
                        {isLoading ? 'Creating Simulation...' : 'Start Simulation'}
                    </button>
                </fieldset>
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
