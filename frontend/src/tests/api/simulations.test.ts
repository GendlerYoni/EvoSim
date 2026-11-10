
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createSimulation, SimulationApiError } from '../../api/simulations'
import type { CreateSimulationRequest } from '../../api/simulationTypes'

// Valid simulation configuration used by the tests.
const validConfig: CreateSimulationRequest = {
    initialHerbivores: 100,
    initialPredators: 10,
    initialFood: 500,
    foodSpawnMaxPerTick: 5
}

// Mock of the browser's fetch function.
const fetchMock = vi.fn()

// Creates a simulated HTTP response.
function mockResponse(status: number, data: unknown) {
    return {
        ok: status >= 200 && status < 300,
        status,
        json: vi.fn().mockResolvedValue(data)
    }
}

describe('createSimulation', () => {

    beforeEach(() => {
        fetchMock.mockReset()
        vi.stubGlobal('fetch', fetchMock)
    })

    afterEach(() => {
        vi.unstubAllGlobals()
    })

    // Successful HTTP requests

    it('sends the correct POST request and returns the session ID', async () => {
        const sessionId = '550e8400-e29b-41d4-a716-446655440000'

        fetchMock.mockResolvedValue(
            mockResponse(201, { sessionId })
        )

        const result = await createSimulation(validConfig)

        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(
            '/api/simulations',
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(validConfig)
            }
        )

        expect(result).toEqual({ sessionId })
    })

    // HTTP errors

    it('handles validation errors (400)', async () => {
        fetchMock.mockResolvedValue(
            mockResponse(400, {
                code: 'VALIDATION_ERROR',
                message: 'Invalid simulation configuration',
                fieldErrors: {
                    initialHerbivores: 'Must be at most 1000'
                }
            })
        )

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            name: 'SimulationApiError',
            status: 400,
            code: 'VALIDATION_ERROR',
            message: 'Invalid simulation configuration',
            fieldErrors: {
                initialHerbivores: 'Must be at most 1000'
            }
        })
    })

    it('handles server capacity errors (503)', async () => {
        fetchMock.mockResolvedValue(
            mockResponse(503, {
                code: 'SERVER_AT_CAPACITY',
                message: 'Server is at capacity',
                fieldErrors: {}
            })
        )

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            status: 503,
            code: 'SERVER_AT_CAPACITY',
            message: 'Server is at capacity'
        })
    })

    it('handles internal server errors (500)', async () => {
        fetchMock.mockResolvedValue(
            mockResponse(500, {
                code: 'INTERNAL_ERROR',
                message: 'An unexpected server error occurred',
                fieldErrors: {}
            })
        )

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            status: 500,
            code: 'INTERNAL_ERROR'
        })
    })

    // Network errors

    it('handles network failures', async () => {
        fetchMock.mockRejectedValue(
            new TypeError('Failed to fetch')
        )

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            name: 'SimulationApiError',
            status: 0,
            code: 'NETWORK_ERROR',
            message: 'Unable to connect to the server',
            fieldErrors: {}
        })
    })

    // Invalid HTTP error responses

    it('handles invalid JSON in an HTTP error response', async () => {
        fetchMock.mockResolvedValue({
            ok: false,
            status: 502,
            json: vi.fn().mockRejectedValue(
                new SyntaxError('Invalid JSON')
            )
        })

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            status: 502,
            code: 'HTTP_ERROR'
        })
    })

    it('handles an HTTP error response with an invalid body structure', async () => {
        fetchMock.mockResolvedValue(
            mockResponse(500, {
                unexpectedField: 'Something went wrong'
            })
        )

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            status: 500,
            code: 'HTTP_ERROR'
        })
    })

    it('handles invalid fieldErrors in an HTTP error response', async () => {
        fetchMock.mockResolvedValue(
            mockResponse(400, {
                code: 'VALIDATION_ERROR',
                message: 'Invalid configuration',
                fieldErrors: {
                    initialHerbivores: 123
                }
            })
        )

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            status: 400,
            code: 'HTTP_ERROR'
        })
    })

    // Invalid successful responses

    it('rejects a successful response without a session ID', async () => {
        fetchMock.mockResolvedValue(
            mockResponse(201, {
                somethingElse: 123
            })
        )

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            status: 201,
            code: 'INVALID_RESPONSE'
        })
    })

    it('rejects a blank session ID', async () => {
        fetchMock.mockResolvedValue(
            mockResponse(201, {
                sessionId: '   '
            })
        )

        await expect(
            createSimulation(validConfig)
        ).rejects.toBeInstanceOf(SimulationApiError)
    })

    it('rejects a non-string session ID', async () => {
        fetchMock.mockResolvedValue(
            mockResponse(201, {
                sessionId: 12345
            })
        )

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            status: 201,
            code: 'INVALID_RESPONSE'
        })
    })

    it('rejects invalid JSON in a successful response', async () => {
        fetchMock.mockResolvedValue({
            ok: true,
            status: 201,
            json: vi.fn().mockRejectedValue(
                new SyntaxError('Invalid JSON')
            )
        })

        await expect(
            createSimulation(validConfig)
        ).rejects.toMatchObject({
            status: 201,
            code: 'INVALID_RESPONSE'
        })
    })
})
