
import type {
    CreateSimulationRequest,
    CreateSimulationResponse,
    ApiErrorResponse
} from './simulationTypes'


export class SimulationApiError extends Error {
    status: number
    code: string
    fieldErrors: Record<string, string>

    constructor(
        status: number,
        code: string,
        message: string,
        fieldErrors: Record<string, string>
    ) {
        super(message)

        this.name = 'SimulationApiError'
        this.status = status
        this.code = code
        this.fieldErrors = fieldErrors
    }
}


// Check whether an HTTP error body matches our API contract.
function isApiErrorResponse(data: unknown): data is ApiErrorResponse {
    if (typeof data !== 'object' || data === null || Array.isArray(data)) {
        return false
    }

    if (
        !('code' in data) ||
        typeof data.code !== 'string' ||
        !('message' in data) ||
        typeof data.message !== 'string' ||
        !('fieldErrors' in data)
    ) {
        return false
    }

    const fieldErrors = data.fieldErrors

    return (
        typeof fieldErrors === 'object' &&
        fieldErrors !== null &&
        !Array.isArray(fieldErrors) &&
        Object.values(fieldErrors).every(value => typeof value === 'string')
    )
}


export async function createSimulation(
    config: CreateSimulationRequest
): Promise<CreateSimulationResponse> {

    let response: Response

    try {
        response = await fetch('/api/simulations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        })
    } catch {
        throw new SimulationApiError(
            0,
            'NETWORK_ERROR',
            'Unable to connect to the server',
            {}
        )
    }

    if (!response.ok) {
        const errorData: unknown = await response.json().catch(() => null)

        if (isApiErrorResponse(errorData)) {
            throw new SimulationApiError(
                response.status,
                errorData.code,
                errorData.message,
                errorData.fieldErrors
            )
        }

        throw new SimulationApiError(
            response.status,
            'HTTP_ERROR',
            `Request failed with HTTP status ${response.status}`,
            {}
        )
    }

    const data: unknown = await response.json().catch(() => null)

    if (
        typeof data !== 'object' ||
        data === null ||
        !('sessionId' in data) ||
        typeof data.sessionId !== 'string' ||
        data.sessionId.trim() === ''
    ) {
        throw new SimulationApiError(
            response.status,
            'INVALID_RESPONSE',
            'Server returned an invalid response',
            {}
        )
    }

    return { sessionId: data.sessionId }
}
