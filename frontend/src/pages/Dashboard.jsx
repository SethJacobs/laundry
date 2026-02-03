import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { Calendar, momentLocalizer } from 'react-big-calendar'
import moment from 'moment'
import 'react-big-calendar/lib/css/react-big-calendar.css'
import toast from 'react-hot-toast'
import BookingModal from '../components/BookingModal'
import MachineStatus from '../components/MachineStatus'

const localizer = momentLocalizer(moment)

function Dashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [showBookingModal, setShowBookingModal] = useState(false)
  const [selectedSlot, setSelectedSlot] = useState(null)

  useEffect(() => {
    fetchBookings()
  }, [])

  const fetchBookings = async () => {
    try {
      const start = moment().startOf('month').toDate()
      const end = moment().endOf('month').add(1, 'month').toDate()
      
      const response = await axios.get('/api/bookings', {
        params: {
          start: start.toISOString(),
          end: end.toISOString()
        }
      })
      
      const events = response.data.map(booking => ({
        id: booking.id,
        title: `${booking.firstName || booking.username} ${booking.lastName || ''}`.trim(),
        start: new Date(booking.startTime),
        end: new Date(booking.endTime),
        resource: booking
      }))
      
      setBookings(events)
    } catch (error) {
      toast.error('Failed to load bookings')
    } finally {
      setLoading(false)
    }
  }

  const handleSelectSlot = ({ start, end }) => {
    setSelectedSlot({ start, end })
    setShowBookingModal(true)
  }

  const handleBookingCreated = () => {
    fetchBookings()
    setShowBookingModal(false)
    setSelectedSlot(null)
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-white"></div>
      </div>
    )
  }

  return (
    <div className="min-h-screen p-4 md:p-8">
      <div className="max-w-7xl mx-auto">
        <div className="bg-white rounded-2xl shadow-2xl p-6 md:p-8">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Laundry Scheduler</h1>
              <p className="text-gray-600 mt-1">Welcome, {user?.username}!</p>
            </div>
            <div className="flex gap-4">
              {user?.isAdmin && (
                <button
                  onClick={() => navigate('/admin')}
                  className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
                >
                  Admin Panel
                </button>
              )}
              <button
                onClick={handleLogout}
                className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
              >
                Logout
              </button>
            </div>
          </div>

          <div className="mb-6 grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 grid grid-cols-1 md:grid-cols-2 gap-4">
              <MachineStatus machineType="washer" />
              <MachineStatus machineType="dryer" />
            </div>
            <div className="lg:col-span-1 flex items-center">
              <button
                onClick={() => {
                  setSelectedSlot(null)
                  setShowBookingModal(true)
                }}
                className="w-full px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium shadow-lg"
              >
                + Book a Time Slot
              </button>
            </div>
          </div>

          <div className="h-[600px]">
            <Calendar
              localizer={localizer}
              events={bookings}
              startAccessor="start"
              endAccessor="end"
              onSelectSlot={handleSelectSlot}
              selectable
              style={{ height: '100%' }}
              defaultView="week"
              views={['month', 'week', 'day']}
            />
          </div>
        </div>
      </div>

      {showBookingModal && (
        <BookingModal
          slot={selectedSlot}
          onClose={() => {
            setShowBookingModal(false)
            setSelectedSlot(null)
          }}
          onSuccess={handleBookingCreated}
        />
      )}
    </div>
  )
}

export default Dashboard

