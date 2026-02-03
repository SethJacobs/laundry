import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { Calendar, momentLocalizer } from 'react-big-calendar'
import moment from 'moment'
import 'react-big-calendar/lib/css/react-big-calendar.css'
import toast from 'react-hot-toast'
import BookingModal from '../components/BookingModal'
import NextAvailableModal from '../components/NextAvailableModal'
import MachineStatus from '../components/MachineStatus'

const localizer = momentLocalizer(moment)

function Dashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [showBookingModal, setShowBookingModal] = useState(false)
  const [selectedSlot, setSelectedSlot] = useState(null)
  const [selectedBooking, setSelectedBooking] = useState(null)
  const [showBookingDetails, setShowBookingDetails] = useState(false)
  const [showNextAvailableModal, setShowNextAvailableModal] = useState(false)

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

  const handleSelectEvent = (event) => {
    setSelectedBooking(event.resource)
    setShowBookingDetails(true)
  }

  const handleBookingCreated = () => {
    fetchBookings()
    setShowBookingModal(false)
    setSelectedSlot(null)
  }

  const handleNextAvailableSuccess = () => {
    fetchBookings()
    setShowNextAvailableModal(false)
  }

  const handleBookNextAvailable = () => {
    setShowNextAvailableModal(true)
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
            <div className="lg:col-span-1 flex flex-col gap-3">
              <button
                onClick={() => {
                  setSelectedSlot(null)
                  setShowBookingModal(true)
                }}
                className="w-full px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium shadow-lg"
              >
                + Book a Time Slot
              </button>
              <button
                onClick={handleBookNextAvailable}
                className="w-full px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium shadow-lg"
              >
                ⚡ Book Next Available
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
              onSelectEvent={handleSelectEvent}
              selectable
              style={{ height: '100%' }}
              defaultView="week"
              views={['month', 'week', 'day']}
              step={60}
              timeslots={1}
              min={new Date(2024, 0, 1, 6, 0, 0)}
              max={new Date(2024, 0, 1, 23, 0, 0)}
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

      {showNextAvailableModal && (
        <NextAvailableModal
          onClose={() => setShowNextAvailableModal(false)}
          onSuccess={handleNextAvailableSuccess}
        />
      )}

      {showBookingDetails && selectedBooking && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">Booking Details</h2>
            
            <div className="space-y-3">
              <div>
                <label className="block text-sm font-medium text-gray-700">Name</label>
                <p className="text-gray-900">{`${selectedBooking.firstName || selectedBooking.username} ${selectedBooking.lastName || ''}`.trim()}</p>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700">Start Time</label>
                <p className="text-gray-900">{moment(selectedBooking.startTime).format('MMMM Do YYYY, h:mm A')}</p>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700">End Time</label>
                <p className="text-gray-900">{moment(selectedBooking.endTime).format('MMMM Do YYYY, h:mm A')}</p>
              </div>
              
              {selectedBooking.notes && (
                <div>
                  <label className="block text-sm font-medium text-gray-700">Notes</label>
                  <p className="text-gray-900">{selectedBooking.notes}</p>
                </div>
              )}
            </div>

            <div className="flex justify-end pt-4">
              <button
                onClick={() => {
                  setShowBookingDetails(false)
                  setSelectedBooking(null)
                }}
                className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default Dashboard

