import React, { useState } from 'react'
import axios from 'axios'
import toast from 'react-hot-toast'
import moment from 'moment'
import { useAuth } from '../context/AuthContext'

function NextAvailableModal({ onClose, onSuccess }) {
  const { user } = useAuth()
  const [duration, setDuration] = useState(120) // Default 2 hours
  const [notes, setNotes] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!user) {
      toast.error('Authentication required')
      return
    }
    
    setLoading(true)

    try {
      const response = await axios.post('/api/bookings/next-available', null, {
        params: {
          durationMinutes: duration,
          notes: notes || 'Auto-booked next available slot'
        }
      })

      toast.success(`Booked next available slot: ${moment(response.data.startTime).format('MMM Do, h:mm A')}`)
      onSuccess()
    } catch (error) {
      toast.error(error.response?.data || 'Failed to find available slot')
    } finally {
      setLoading(false)
    }
  }

  const durationOptions = [
    { value: 60, label: '1 hour' },
    { value: 90, label: '1.5 hours' },
    { value: 120, label: '2 hours' },
    { value: 150, label: '2.5 hours' },
    { value: 180, label: '3 hours' },
    { value: 240, label: '4 hours' }
  ]

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Book Next Available Slot</h2>
        <p className="text-gray-600 mb-4">Find and book the next available time slot automatically.</p>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Duration
            </label>
            <select
              value={duration}
              onChange={(e) => setDuration(parseInt(e.target.value))}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            >
              {durationOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Notes (optional)
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows="3"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
              placeholder="Any special notes..."
            />
          </div>

          <div className="bg-blue-50 p-3 rounded-md">
            <p className="text-sm text-blue-800">
              <strong>Search criteria:</strong>
              <br />• Operating hours: 6 AM - 11 PM
              <br />• Search period: Next 7 days
              <br />• Minimum gap: 30 minutes from now
            </p>
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Finding...' : 'Find & Book'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default NextAvailableModal