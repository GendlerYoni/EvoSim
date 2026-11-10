export type CreateSimulationRequest = {
    initialHerbivores: number,
    initialPredators: number,
    initialFood: number,
    foodSpawnMaxPerTick: number
}

export type CreateSimulationResponse = {
    sessionId: string
}

export type ApiErrorResponse = {
    code: string,
    message: string,
    fieldErrors: Record<string, string>
}