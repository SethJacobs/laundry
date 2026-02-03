import React, { useState } from 'react'
import axios from 'axios'
import toast from 'react-hot-toast'
import moment from 'moment'
import { useAuth } from '../context/AuthContext'

function BookingModal({ slot, onClose, onSuccess }) {
  const { user } = useAuth()
  const [startTime, setStartTime] = useState(
    slot?.start ? moment(slot.start).format('YYYY-MM-DDTHH:mm') : ''
  )
  const [endTime, setEndTime] = useState(
    slot?.end ? moment(slot.end).format('YYYY-MM-DDTHH:mm') : ''
  )
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
      await axios.post('/api/bookings', {
        startTime: new Date(startTime).toISOString(),
        endTime: new Date(endTime).toISOString(),
        notes
      })

      toast.success('Booking created successfully!')
      onSuccess()
    } catch (error) {
      toast.error(error.response?.data || 'Failed to create booking')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Book Laundry Time</h2>
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Start Time
            </label>
            <input
              type="datetime-local"
              required
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              End Time
            </label>
            <input
              type="datetime-local"
              required
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
            />
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
              className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Booking...' : 'Book'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default BookingModal

