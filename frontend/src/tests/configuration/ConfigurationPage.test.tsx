
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react'

import ConfigurationPage from '../../features/configuration/ConfigurationPage'
import { createSimulation, SimulationApiError } from '../../api/simulations'
import type { CreateSimulationResponse } from '../../api/simulationTypes'

// Mock the API module, so component tests never send real HTTP requests.
vi.mock('../../api/simulations', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/simulations')>()

  return {
    ...actual,
    createSimulation: vi.fn()
  }
})

const createSimulationMock = vi.mocked(createSimulation)

// Keeps a request pending until a test explicitly resolves or rejects it.
function deferredSimulation() {
  let resolve!: (result: CreateSimulationResponse) => void
  let reject!: (reason?: unknown) => void

  const promise = new Promise<CreateSimulationResponse>((res, rej) => {
    resolve = res
    reject = rej
  })

  return { promise, resolve, reject }
}

beforeEach(() => {
  createSimulationMock.mockReset()
  sessionStorage.clear()

  // Suppress temporary success logging during tests.
  vi.spyOn(console, 'log').mockImplementation(() => { })
})

afterEach(() => {
  vi.restoreAllMocks()
  sessionStorage.clear()
})

describe('ConfigurationPage', () => {

  // Initial configuration

  it('renders the default configuration values', () => {
    render(<ConfigurationPage />)

    expect(
      screen.getByRole('textbox', { name: 'Initial Herbivores' })
    ).toHaveValue('100')

    expect(
      screen.getByRole('textbox', { name: 'Initial Predators' })
    ).toHaveValue('100')

    expect(
      screen.getByRole('textbox', { name: 'Initial Food' })
    ).toHaveValue('100')

    expect(
      screen.getByRole('textbox', { name: 'Max Food Spawn per Tick' })
    ).toHaveValue('5')
  })

  // Form validation

  it('shows an error when submitting an empty field', () => {
    render(<ConfigurationPage />)

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Initial Herbivores' }),
      { target: { value: '' } }
    )

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    expect(screen.getByRole('alert'))
      .toHaveTextContent('This field is required')

    expect(createSimulationMock).not.toHaveBeenCalled()
  })

  it('shows errors for multiple empty fields', () => {
    render(<ConfigurationPage />)

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Initial Herbivores' }),
      { target: { value: '' } }
    )

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Initial Predators' }),
      { target: { value: '' } }
    )

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    const errors = screen.getAllByRole('alert')

    expect(errors).toHaveLength(2)

    errors.forEach(error => {
      expect(error).toHaveTextContent('This field is required')
    })

    expect(createSimulationMock).not.toHaveBeenCalled()
  })

  it('clears a validation error when the field is corrected', () => {
    render(<ConfigurationPage />)

    const herbivoresInput = screen.getByRole('textbox', {
      name: 'Initial Herbivores'
    })

    fireEvent.change(herbivoresInput, {
      target: { value: '' }
    })

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    expect(screen.getByRole('alert'))
      .toHaveTextContent('This field is required')

    fireEvent.change(herbivoresInput, {
      target: { value: '200' }
    })

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  // Successful form submission

  it('submits the default configuration as numbers', async () => {
    const sessionId = 'test-session-123'

    createSimulationMock.mockResolvedValue({ sessionId })

    render(<ConfigurationPage />)

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    expect(createSimulationMock).toHaveBeenCalledExactlyOnceWith({
      initialHerbivores: 100,
      initialPredators: 100,
      initialFood: 100,
      foodSpawnMaxPerTick: 5
    })

    await waitFor(() => {
      expect(sessionStorage.getItem('sessionId')).toBe(sessionId)
      expect(
        screen.getByRole('button', { name: 'Start Simulation' })
      ).toBeEnabled()
    })
  })

  it('submits modified configuration values', async () => {
    const sessionId = 'modified-session-123'

    createSimulationMock.mockResolvedValue({ sessionId })

    render(<ConfigurationPage />)

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Initial Herbivores' }),
      { target: { value: '250' } }
    )

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Initial Predators' }),
      { target: { value: '50' } }
    )

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Initial Food' }),
      { target: { value: '500' } }
    )

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Max Food Spawn per Tick' }),
      { target: { value: '10' } }
    )

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    expect(createSimulationMock).toHaveBeenCalledExactlyOnceWith({
      initialHerbivores: 250,
      initialPredators: 50,
      initialFood: 500,
      foodSpawnMaxPerTick: 10
    })

    await waitFor(() => {
      expect(sessionStorage.getItem('sessionId')).toBe(sessionId)
    })
  })

  // Reset behavior

  it('resets modified configuration to defaults', () => {
    render(<ConfigurationPage />)

    const herbivoresInput = screen.getByRole('textbox', {
      name: 'Initial Herbivores'
    })

    fireEvent.change(herbivoresInput, {
      target: { value: '250' }
    })

    expect(herbivoresInput).toHaveValue('250')

    fireEvent.click(
      screen.getByRole('button', { name: 'Reset to Defaults' })
    )

    // Reset remounts NumberSetting, so query the new input.
    expect(
      screen.getByRole('textbox', { name: 'Initial Herbivores' })
    ).toHaveValue('100')
  })

  it('clears validation errors when resetting the form', () => {
    render(<ConfigurationPage />)

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Initial Herbivores' }),
      { target: { value: '' } }
    )

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    expect(screen.getByRole('alert'))
      .toHaveTextContent('This field is required')

    fireEvent.click(
      screen.getByRole('button', { name: 'Reset to Defaults' })
    )

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()

    expect(
      screen.getByRole('textbox', { name: 'Initial Herbivores' })
    ).toHaveValue('100')
  })

  it('clears local notices when resetting the form', () => {
    render(<ConfigurationPage />)

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Initial Herbivores' }),
      { target: { value: '2000' } }
    )

    expect(screen.getByRole('status'))
      .toHaveTextContent('Value adjusted to 1000')

    fireEvent.click(
      screen.getByRole('button', { name: 'Reset to Defaults' })
    )

    expect(screen.queryByRole('status')).not.toBeInTheDocument()

    expect(
      screen.getByRole('textbox', { name: 'Initial Herbivores' })
    ).toHaveValue('100')
  })

  // Loading behavior

  it('disables all configuration controls while creating a simulation', async () => {
    let resolveRequest!: (result: CreateSimulationResponse) => void

    createSimulationMock.mockImplementation(() => {
      return new Promise<CreateSimulationResponse>(resolve => {
        resolveRequest = resolve
      })
    })

    render(<ConfigurationPage />)

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    expect(
      screen.getByRole('button', { name: 'Creating Simulation...' })
    ).toBeDisabled()

    expect(
      screen.getByRole('button', { name: 'Reset to Defaults' })
    ).toBeDisabled()

    expect(
      screen.getByRole('textbox', { name: 'Initial Herbivores' })
    ).toBeDisabled()

    expect(
      screen.getByRole('slider', { name: 'Initial Herbivores slider' })
    ).toBeDisabled()

    expect(
      screen.getByRole('button', { name: 'Increase Initial Herbivores' })
    ).toBeDisabled()

    expect(
      screen.getByRole('button', { name: 'Decrease Initial Herbivores' })
    ).toBeDisabled()

    expect(screen.getAllByRole('textbox'))
      .toHaveLength(4)

    screen.getAllByRole('textbox').forEach(input => {
      expect(input).toBeDisabled()
    })

    screen.getAllByRole('slider').forEach(slider => {
      expect(slider).toBeDisabled()
    })

    await act(async () => {
      resolveRequest({ sessionId: 'loading-test-123' })
    })

    expect(
      screen.getByRole('button', { name: 'Start Simulation' })
    ).toBeEnabled()

    expect(
      screen.getByRole('button', { name: 'Reset to Defaults' })
    ).toBeEnabled()

    expect(sessionStorage.getItem('sessionId'))
      .toBe('loading-test-123')
  })

  it('does not submit another request while loading', async () => {
    let resolveRequest!: (result: CreateSimulationResponse) => void

    createSimulationMock.mockImplementation(() => {
      return new Promise<CreateSimulationResponse>(resolve => {
        resolveRequest = resolve
      })
    })

    render(<ConfigurationPage />)

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    const loadingButton = screen.getByRole('button', {
      name: 'Creating Simulation...'
    })

    expect(loadingButton).toBeDisabled()

    fireEvent.click(loadingButton)

    expect(createSimulationMock).toHaveBeenCalledTimes(1)

    await act(async () => {
      resolveRequest({ sessionId: 'single-request-123' })
    })

    expect(createSimulationMock).toHaveBeenCalledTimes(1)
  })

  // Failed requests and visible error messages

  it('unlocks the form, displays an error, and does not save a session after failure', async () => {
    createSimulationMock.mockRejectedValueOnce(new Error('Unexpected failure'))

    render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'An unexpected error occurred. Please try again.'
    )
    expect(screen.getByRole('button', { name: 'Start Simulation' })).toBeEnabled()
    expect(screen.getByRole('textbox', { name: 'Initial Herbivores' })).toBeEnabled()
    expect(sessionStorage.getItem('sessionId')).toBeNull()
  })

  it('displays a friendly message for a network error', async () => {
    createSimulationMock.mockRejectedValueOnce(
      new SimulationApiError(0, 'NETWORK_ERROR', 'Network request failed', {})
    )

    render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Unable to connect to the server. Please try again.'
    )
  })

  it('displays the server-capacity message', async () => {
    createSimulationMock.mockRejectedValueOnce(
      new SimulationApiError(503, 'SERVER_AT_CAPACITY', 'Capacity reached', {})
    )

    render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'The server is at capacity. Please try again later.'
    )
  })

  it('displays server field errors without a duplicate general alert', async () => {
    createSimulationMock.mockRejectedValueOnce(
      new SimulationApiError(400, 'VALIDATION_ERROR', 'Invalid configuration', {
        initialHerbivores: 'Herbivores are not allowed',
        initialFood: 'Food count is invalid'
      })
    )

    render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    await waitFor(() => {
      expect(screen.getAllByRole('alert')).toHaveLength(2)
    })

    expect(screen.getByText('Herbivores are not allowed')).toBeInTheDocument()
    expect(screen.getByText('Food count is invalid')).toBeInTheDocument()
    expect(screen.queryByText('Invalid configuration')).not.toBeInTheDocument()
  })

  it('clears only the corrected field server error', async () => {
    createSimulationMock.mockRejectedValueOnce(
      new SimulationApiError(400, 'VALIDATION_ERROR', 'Invalid configuration', {
        initialHerbivores: 'Invalid herbivores',
        initialFood: 'Invalid food'
      })
    )

    render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    expect(await screen.findByText('Invalid herbivores')).toBeInTheDocument()
    expect(screen.getByText('Invalid food')).toBeInTheDocument()

    fireEvent.change(screen.getByRole('textbox', { name: 'Initial Herbivores' }), {
      target: { value: '150' }
    })

    expect(screen.queryByText('Invalid herbivores')).not.toBeInTheDocument()
    expect(screen.getByText('Invalid food')).toBeInTheDocument()
  })

  it('clears a general request error when a field changes', async () => {
    createSimulationMock.mockRejectedValueOnce(new Error('Failure'))

    render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    expect(await screen.findByRole('alert')).toBeInTheDocument()

    fireEvent.change(screen.getByRole('textbox', { name: 'Initial Herbivores' }), {
      target: { value: '150' }
    })

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('clears a general request error when resetting', async () => {
    createSimulationMock.mockRejectedValueOnce(new Error('Failure'))

    render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    expect(await screen.findByRole('alert')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Reset to Defaults' }))

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('clears server field errors when resetting', async () => {
    createSimulationMock.mockRejectedValueOnce(
      new SimulationApiError(400, 'VALIDATION_ERROR', 'Invalid configuration', {
        initialPredators: 'Invalid predator count'
      })
    )

    render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    expect(await screen.findByText('Invalid predator count')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Reset to Defaults' }))

    expect(screen.queryByText('Invalid predator count')).not.toBeInTheDocument()
  })

  it('clears the old error while retrying and stores the new session on success', async () => {
    const retry = deferredSimulation()
    createSimulationMock
      .mockRejectedValueOnce(new Error('First attempt failed'))
      .mockReturnValueOnce(retry.promise)

    render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    expect(await screen.findByRole('alert')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Creating Simulation...' })).toBeDisabled()
    expect(createSimulationMock).toHaveBeenCalledTimes(2)

    await act(async () => {
      retry.resolve({ sessionId: 'retry-success-123' })
    })

    expect(sessionStorage.getItem('sessionId')).toBe('retry-success-123')
  })

  // Request concurrency: the ref must block submits even if submit is dispatched directly.

  it('blocks concurrent programmatic form submits', async () => {
    const pending = deferredSimulation()
    createSimulationMock.mockReturnValueOnce(pending.promise)

    const { container } = render(<ConfigurationPage />)
    const form = container.querySelector('form')!

    fireEvent.submit(form)
    fireEvent.submit(form)

    expect(createSimulationMock).toHaveBeenCalledTimes(1)

    await act(async () => {
      pending.resolve({ sessionId: 'single-request-123' })
    })
  })

  // Unmount behavior: ignore responses after leaving the configuration page.

  it('does not store a session when a pending request succeeds after unmount', async () => {
    const pending = deferredSimulation()
    createSimulationMock.mockReturnValueOnce(pending.promise)

    const { unmount } = render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))
    expect(createSimulationMock).toHaveBeenCalledTimes(1)

    unmount()

    await act(async () => {
      pending.resolve({ sessionId: 'too-late-123' })
    })

    expect(sessionStorage.getItem('sessionId')).toBeNull()
  })

  it('handles a pending request failure after unmount', async () => {
    const pending = deferredSimulation()
    createSimulationMock.mockReturnValueOnce(pending.promise)

    const { unmount } = render(<ConfigurationPage />)
    fireEvent.click(screen.getByRole('button', { name: 'Start Simulation' }))
    expect(createSimulationMock).toHaveBeenCalledTimes(1)

    unmount()

    await act(async () => {
      pending.reject(new Error('Too late'))
    })

    expect(sessionStorage.getItem('sessionId')).toBeNull()
  })
})
