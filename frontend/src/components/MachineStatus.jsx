import React, { useState, useEffect } from 'react'
import axios from 'axios'
import { useAuth } from '../context/AuthContext'

function MachineStatus({ machineType = 'washer' }) {
  const { user, loading: authLoading } = useAuth()
  const [status, setStatus] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Don't fetch if still authenticating or user not authenticated
    if (authLoading || !user) {
      return
    }
    
    console.log(`[MachineStatus] Initializing ${machineType} status component`)
    fetchStatus()
    const interval = setInterval(fetchStatus, 10000) // Poll every 10 seconds
    return () => clearInterval(interval)
  }, [machineType, user, authLoading])

  const fetchStatus = async () => {
    console.log(`[MachineStatus] Fetching ${machineType} status from /api/machines/${machineType}`)
    console.log(`[MachineStatus] Current axios auth header:`, axios.defaults.headers.common['Authorization'])
    console.log(`[MachineStatus] User authenticated:`, !!user)
    
    try {
      const response = await axios.get(`/api/machines/${machineType}`)
      console.log(`[MachineStatus] ${machineType} response:`, response.data)
      setStatus(response.data)
      if (response.data && !response.data.enabled) {
        console.warn(`${machineType} status: Home Assistant not enabled or configured`)
      }
    } catch (error) {
      console.error(`Failed to fetch ${machineType} status:`, error)
      console.error('Error details:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
        url: error.config?.url
      })
      // Set a status object to show error state
      setStatus({
        enabled: false,
        error: true,
        errorMessage: error.response?.status === 401 
          ? 'Authentication required' 
          : error.response?.status === 403
          ? 'Access forbidden'
          : 'Connection error'
      })
    } finally {
      setLoading(false)
    }
  }

  const machineName = machineType === 'washer' ? 'Washing Machine' : 'Dryer'
  const gradientColors = machineType === 'washer' 
    ? 'from-blue-50 to-indigo-50 border-blue-200' 
    : 'from-orange-50 to-amber-50 border-orange-200'
  const iconColor = machineType === 'washer' ? 'text-blue-600' : 'text-orange-600'
  const progressColor = machineType === 'washer' ? 'bg-blue-600' : 'bg-orange-600'

  if (authLoading || loading) {
    return (
      <div className="bg-white rounded-xl shadow-lg p-6 animate-pulse">
        <div className="h-4 bg-gray-200 rounded w-1/3 mb-4"></div>
        <div className="h-8 bg-gray-200 rounded w-1/2"></div>
      </div>
    )
  }

  if (!status || !status.enabled) {
    return (
      <div className="bg-gray-100 rounded-xl shadow-lg p-6 border-2 border-dashed border-gray-300">
        <div className="flex items-center gap-3">
          <div className="text-4xl">🏠</div>
          <div>
            <h3 className="text-lg font-semibold text-gray-700">{machineName}</h3>
            {status?.error ? (
              <div>
                <p className="text-sm text-red-600 font-medium">{status.errorMessage || 'Connection error'}</p>
                <p className="text-xs text-gray-500 mt-1">Check browser console for details</p>
              </div>
            ) : (
              <p className="text-sm text-gray-500">Not configured</p>
            )}
          </div>
        </div>
      </div>
    )
  }

  const isRunning = status.running
  const timeRemaining = status.timeRemainingMinutes
  const statusText = status.status || 'unknown'

  const getStatusColor = () => {
    if (isRunning) return machineType === 'washer' ? 'bg-blue-500' : 'bg-orange-500'
    if (statusText === 'finished') return 'bg-green-500'
    if (statusText === 'idle') return 'bg-gray-400'
    return 'bg-yellow-500'
  }

  const getStatusIcon = () => {
    if (isRunning) return '🔄'
    if (statusText === 'finished') return '✅'
    if (statusText === 'idle') return '💤'
    return '❓'
  }

  return (
    <div className={`bg-gradient-to-br ${gradientColors} rounded-xl shadow-xl p-6 border-2`}>
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <div className="text-5xl animate-bounce">{getStatusIcon()}</div>
          <div>
            <h3 className="text-xl font-bold text-gray-800">{machineName}</h3>
            <p className="text-sm text-gray-600 capitalize">{statusText}</p>
          </div>
        </div>
        <div className={`px-4 py-2 rounded-full ${getStatusColor()} text-white font-semibold text-sm shadow-lg transform transition-all ${
          isRunning ? 'animate-pulse' : ''
        }`}>
          {isRunning ? 'RUNNING' : statusText.toUpperCase()}
        </div>
      </div>

      {isRunning && (
        <div className="mt-4 p-4 bg-white rounded-lg shadow-inner">
          <div className="flex items-center justify-between">
            <span className="text-gray-700 font-medium">Time Remaining:</span>
            <div className="flex items-center gap-2">
              {timeRemaining !== null ? (
                <>
                  <span className={`text-3xl font-bold ${iconColor}`}>{timeRemaining}</span>
                  <span className="text-gray-600">min</span>
                </>
              ) : (
                <span className="text-gray-500 italic">Unknown</span>
              )}
            </div>
          </div>
          {timeRemaining !== null && (
            <div className="mt-3">
              <div className="w-full bg-gray-200 rounded-full h-2.5">
                <div
                  className={`${progressColor} h-2.5 rounded-full transition-all duration-1000`}
                  style={{
                    width: `${Math.min(100, (timeRemaining / 60) * 100)}%`
                  }}
                ></div>
              </div>
            </div>
          )}
        </div>
      )}

      {!isRunning && statusText === 'finished' && (
        <div className="mt-4 p-4 bg-green-50 rounded-lg border-2 border-green-200">
          <p className="text-green-800 font-medium text-center">
            ✨ Cycle complete! Ready for next load
          </p>
        </div>
      )}

      {!isRunning && statusText === 'idle' && (
        <div className="mt-4 p-4 bg-gray-50 rounded-lg border-2 border-gray-200">
          <p className="text-gray-700 font-medium text-center">
            💤 Machine is idle and available
          </p>
        </div>
      )}

      <div className="mt-4 text-xs text-gray-500 text-center">
        Last updated: {new Date().toLocaleTimeString()}
      </div>
    </div>
  )
}

export default MachineStatus

