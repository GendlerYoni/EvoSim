
import { useEffect, useRef, useState } from 'react'

type NumberSettingProps = {
    label: string
    value: string
    min: number
    max: number
    onChange: (value: string) => void
    error?: string
}

const HOLD_DELAY_MS = 400
const HOLD_REPEAT_MS = 80

function NumberSetting({
    label,
    value,
    min,
    max,
    onChange,
    error
}: NumberSettingProps) {
    // State and refs
    const [notice, setNotice] = useState('')

    const latestValueRef = useRef(value)
    const holdTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)
    const holdIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null)
    const didHoldRef = useRef(false)

    // Keep the ref synchronized with the value from the parent
    useEffect(() => {
        latestValueRef.current = value
    }, [value])

    // Clean up timers when the component unmounts
    useEffect(() => {
        return () => {
            if (holdTimeoutRef.current !== null) {
                clearTimeout(holdTimeoutRef.current)
            }

            if (holdIntervalRef.current !== null) {
                clearInterval(holdIntervalRef.current)
            }
        }
    }, [])

    // Value updates
    function updateValue(newValue: string, newNotice = '') {
        latestValueRef.current = newValue
        setNotice(newNotice)
        onChange(newValue)
    }

    function handleTextChange(rawValue: string) {
        if (rawValue === '') {
            updateValue('')
            return
        }

        if (!/^\d+$/.test(rawValue)) {
            setNotice('Only whole numbers are allowed')
            return
        }

        const number = Number(rawValue)

        if (number < min || number > max) {
            const clamped = Math.max(min, Math.min(max, number))

            updateValue(String(clamped), `Value adjusted to ${clamped}`)
            return
        }

        updateValue(rawValue)
    }

    function changeBy(delta: number) {
        const currentValue = latestValueRef.current
        const current = Number(currentValue)

        if (currentValue.trim() === '' || !Number.isInteger(current)) {
            return
        }

        const next = Math.max(min, Math.min(max, current + delta))

        updateValue(String(next))
    }

    // Press-and-hold behavior
    function stopHold() {
        if (holdTimeoutRef.current !== null) {
            clearTimeout(holdTimeoutRef.current)
            holdTimeoutRef.current = null
        }

        if (holdIntervalRef.current !== null) {
            clearInterval(holdIntervalRef.current)
            holdIntervalRef.current = null
        }
    }

    function startHold(delta: number) {
        stopHold()
        didHoldRef.current = false

        holdTimeoutRef.current = setTimeout(() => {
            didHoldRef.current = true
            holdTimeoutRef.current = null

            changeBy(delta)

            holdIntervalRef.current = setInterval(() => {
                changeBy(delta)
            }, HOLD_REPEAT_MS)
        }, HOLD_DELAY_MS)
    }

    function handleClick(delta: number, isKeyboardClick: boolean) {
        if (isKeyboardClick || !didHoldRef.current) {
            changeBy(delta)
        }

        didHoldRef.current = false
    }

    return (
        <div>
            <label>
                {label}
                <input
                    type="text"
                    inputMode="numeric"
                    value={value}
                    onChange={(event) => handleTextChange(event.currentTarget.value)}
                />
            </label>

            {notice && <p role="status">{notice}</p>}
            {error && <p role="alert">{error}</p>}

            <input
                type="range"
                min={min}
                max={max}
                step={1}
                value={value}
                aria-label={`${label} slider`}
                onChange={(event) => updateValue(event.currentTarget.value)}
            />

            <button
                type="button"
                aria-label={`Decrease ${label}`}
                onClick={(event) => handleClick(-1, event.detail === 0)}
                onPointerDown={(event) => {
                    if (event.isPrimary && event.button === 0) {
                        startHold(-1)
                    }
                }}
                onPointerUp={stopHold}
                onPointerLeave={stopHold}
                onPointerCancel={stopHold}
            >
                -
            </button>

            <button
                type="button"
                aria-label={`Increase ${label}`}
                onClick={(event) => handleClick(1, event.detail === 0)}
                onPointerDown={(event) => {
                    if (event.isPrimary && event.button === 0) {
                        startHold(1)
                    }
                }}
                onPointerUp={stopHold}
                onPointerLeave={stopHold}
                onPointerCancel={stopHold}
            >
                +
            </button>
        </div>
    )
}

export default NumberSetting
