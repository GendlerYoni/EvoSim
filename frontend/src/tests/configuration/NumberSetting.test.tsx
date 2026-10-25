import { useState } from 'react'
import { act, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import NumberSetting from '../../features/configuration/NumberSetting'

function renderNumberSetting(
    initialValue = '5',
    error?: string
) {
    function TestComponent() {
        const [value, setValue] = useState(initialValue)

        return (
            <NumberSetting
                label="Initial Herbivores"
                value={value}
                min={0}
                max={10}
                onChange={setValue}
                error={error}
            />
        )
    }

    render(<TestComponent />)

    return {
        input: screen.getByRole('textbox', {
            name: 'Initial Herbivores'
        }),
        slider: screen.getByRole('slider', {
            name: 'Initial Herbivores slider'
        }),
        increaseButton: screen.getByRole('button', {
            name: 'Increase Initial Herbivores'
        }),
        decreaseButton: screen.getByRole('button', {
            name: 'Decrease Initial Herbivores'
        })
    }
}

describe('NumberSetting', () => {
    it('renders the initial value', () => {
        const { input, slider } = renderNumberSetting('5')

        expect(input).toHaveValue('5')
        expect(slider).toHaveValue('5')
    })

    it('updates the value when entering a valid number', () => {
        const { input, slider } = renderNumberSetting()

        fireEvent.change(input, {
            target: { value: '7' }
        })

        expect(input).toHaveValue('7')
        expect(slider).toHaveValue('7')
    })

    it('rejects non-integer input', () => {
        const { input } = renderNumberSetting()

        fireEvent.change(input, {
            target: { value: '3.5' }
        })

        expect(input).toHaveValue('5')
        expect(screen.getByRole('status'))
            .toHaveTextContent('Only whole numbers are allowed')
    })

    it('clamps values above the maximum', () => {
        const { input } = renderNumberSetting()

        fireEvent.change(input, {
            target: { value: '15' }
        })

        expect(input).toHaveValue('10')
        expect(screen.getByRole('status'))
            .toHaveTextContent('Value adjusted to 10')
    })

    it('allows an empty value temporarily', () => {
        const { input } = renderNumberSetting()

        fireEvent.change(input, {
            target: { value: '' }
        })

        expect(input).toHaveValue('')
    })

    it('updates the value using the slider', () => {
        const { input, slider } = renderNumberSetting()

        fireEvent.change(slider, {
            target: { value: '8' }
        })

        expect(input).toHaveValue('8')
    })

    it('increments and decrements using the buttons', () => {
        const { input, increaseButton, decreaseButton } =
            renderNumberSetting()

        fireEvent.click(increaseButton)
        expect(input).toHaveValue('6')

        fireEvent.click(decreaseButton)
        expect(input).toHaveValue('5')
    })

    it('does not exceed the configured maximum', () => {
        const { input, increaseButton } = renderNumberSetting('10')

        fireEvent.click(increaseButton)

        expect(input).toHaveValue('10')
    })

    it('displays validation errors received from the parent', () => {
        renderNumberSetting('', 'This field is required')

        expect(screen.getByRole('alert'))
            .toHaveTextContent('This field is required')
    })

    describe('NumberSetting press-and-hold', () => {
        beforeEach(() => {
            vi.useFakeTimers()
        })

        afterEach(() => {
            vi.clearAllTimers()
            vi.useRealTimers()
        })

        it('increments repeatedly after the hold delay', () => {
            const { input, increaseButton } = renderNumberSetting('5')

            fireEvent.pointerDown(increaseButton, {
                button: 0,
                isPrimary: true
            })

            act(() => {
                vi.advanceTimersByTime(399)
            })

            expect(input).toHaveValue('5')

            act(() => {
                vi.advanceTimersByTime(1)
            })

            expect(input).toHaveValue('6')

            act(() => {
                vi.advanceTimersByTime(160)
            })

            expect(input).toHaveValue('8')

            fireEvent.pointerUp(increaseButton)

            act(() => {
                vi.advanceTimersByTime(400)
            })

            expect(input).toHaveValue('8')
        })

        it('increments only once on a short press', () => {
            const { input, increaseButton } = renderNumberSetting('5')

            fireEvent.pointerDown(increaseButton, {
                button: 0,
                isPrimary: true
            })

            act(() => {
                vi.advanceTimersByTime(100)
            })

            fireEvent.pointerUp(increaseButton)
            fireEvent.click(increaseButton, { detail: 1 })

            expect(input).toHaveValue('6')

            act(() => {
                vi.advanceTimersByTime(500)
            })

            expect(input).toHaveValue('6')
        })

        it('stops repeating when the pointer leaves the button', () => {
            const { input, increaseButton } = renderNumberSetting('5')

            fireEvent.pointerDown(increaseButton, {
                button: 0,
                isPrimary: true
            })

            act(() => {
                vi.advanceTimersByTime(400)
            })

            expect(input).toHaveValue('6')

            fireEvent.pointerLeave(increaseButton)

            act(() => {
                vi.advanceTimersByTime(500)
            })

            expect(input).toHaveValue('6')
        })

        it('cleans up active timers when unmounted', () => {
            const onChange = vi.fn()

            const { unmount } = render(
                <NumberSetting
                    label="Test Setting"
                    value="5"
                    min={0}
                    max={10}
                    onChange={onChange}
                />
            )

            const button = screen.getByRole('button', {
                name: 'Increase Test Setting'
            })

            fireEvent.pointerDown(button, {
                button: 0,
                isPrimary: true
            })

            act(() => {
                vi.advanceTimersByTime(400)
            })

            expect(vi.getTimerCount()).toBeGreaterThan(0)

            unmount()

            expect(vi.getTimerCount()).toBe(0)
        })
    })

})
