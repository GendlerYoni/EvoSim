
import { afterEach, describe, expect, it, vi } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'

import ConfigurationPage from '../../features/configuration/ConfigurationPage'

afterEach(() => {
  vi.restoreAllMocks()
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

    const consoleSpy = vi.spyOn(console, 'log')
      .mockImplementation(() => {})

    fireEvent.change(
      screen.getByRole('textbox', { name: 'Initial Herbivores' }),
      { target: { value: '' } }
    )

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    expect(screen.getByRole('alert'))
      .toHaveTextContent('This field is required')

    expect(consoleSpy).not.toHaveBeenCalled()
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

  // Form submission

  it('submits the default configuration as numbers', () => {
    render(<ConfigurationPage />)

    const consoleSpy = vi.spyOn(console, 'log')
      .mockImplementation(() => {})

    fireEvent.click(
      screen.getByRole('button', { name: 'Start Simulation' })
    )

    expect(consoleSpy).toHaveBeenCalledExactlyOnceWith({
      initialHerbivores: 100,
      initialPredators: 100,
      initialFood: 100,
      foodSpawnMaxPerTick: 5
    })
  })

  it('submits modified configuration values', () => {
    render(<ConfigurationPage />)

    const consoleSpy = vi.spyOn(console, 'log')
      .mockImplementation(() => {})

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

    expect(consoleSpy).toHaveBeenCalledExactlyOnceWith({
      initialHerbivores: 250,
      initialPredators: 50,
      initialFood: 500,
      foodSpawnMaxPerTick: 10
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

    // Reset remounts NumberSetting, so query the new input
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
})
