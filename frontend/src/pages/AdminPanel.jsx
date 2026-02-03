import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import toast from 'react-hot-toast'
import moment from 'moment'

function AdminPanel() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedUser, setSelectedUser] = useState(null)
  const [blockUntil, setBlockUntil] = useState('')
  const [blockReason, setBlockReason] = useState('')

  useEffect(() => {
    if (!user?.isAdmin) {
      navigate('/dashboard')
      return
    }
    fetchUsers()
  }, [user, navigate])

  const fetchUsers = async () => {
    try {
      const response = await axios.get('/api/admin/users')
      setUsers(response.data)
    } catch (error) {
      toast.error('Failed to load users')
    } finally {
      setLoading(false)
    }
  }

  const handleBlockUser = async () => {
    if (!selectedUser) return

    try {
      await axios.post('/api/admin/block-user', {
        userId: selectedUser.id,
        blockedUntil: blockUntil ? new Date(blockUntil).toISOString() : null,
        reason: blockReason
      })

      toast.success('User blocked successfully')
      fetchUsers()
      setSelectedUser(null)
      setBlockUntil('')
      setBlockReason('')
    } catch (error) {
      toast.error('Failed to block user')
    }
  }

  const handleUnblockUser = async (userId) => {
    try {
      await axios.post(`/api/admin/unblock-user/${userId}`)
      toast.success('User unblocked successfully')
      fetchUsers()
    } catch (error) {
      toast.error('Failed to unblock user')
    }
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
      <div className="max-w-6xl mx-auto">
        <div className="bg-white rounded-2xl shadow-2xl p-6 md:p-8">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-3xl font-bold text-gray-900">Admin Panel</h1>
            <div className="flex gap-4">
              <button
                onClick={() => navigate('/dashboard')}
                className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                Dashboard
              </button>
              <button
                onClick={() => {
                  logout()
                  navigate('/login')
                }}
                className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
              >
                Logout
              </button>
            </div>
          </div>

          <div className="mb-6">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Users</h2>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Username
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Email
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Blocked Until
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {u.username}
                        {u.isAdmin && (
                          <span className="ml-2 px-2 py-1 text-xs bg-purple-100 text-purple-800 rounded">
                            Admin
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {u.email}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {u.isBlocked ? (
                          <span className="px-2 py-1 text-xs bg-red-100 text-red-800 rounded">
                            Blocked
                          </span>
                        ) : (
                          <span className="px-2 py-1 text-xs bg-green-100 text-green-800 rounded">
                            Active
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {u.blockedUntil
                          ? moment(u.blockedUntil).format('MMM DD, YYYY HH:mm')
                          : '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        {u.isBlocked ? (
                          <button
                            onClick={() => handleUnblockUser(u.id)}
                            className="text-green-600 hover:text-green-900"
                          >
                            Unblock
                          </button>
                        ) : (
                          <button
                            onClick={() => setSelectedUser(u)}
                            className="text-red-600 hover:text-red-900"
                          >
                            Block
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {selectedUser && (
            <div className="mt-6 p-6 bg-gray-50 rounded-lg">
              <h3 className="text-lg font-semibold mb-4">
                Block User: {selectedUser.username}
              </h3>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Block Until (leave empty for permanent)
                  </label>
                  <input
                    type="datetime-local"
                    value={blockUntil}
                    onChange={(e) => setBlockUntil(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Reason
                  </label>
                  <textarea
                    value={blockReason}
                    onChange={(e) => setBlockReason(e.target.value)}
                    rows="3"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500"
                    placeholder="Reason for blocking..."
                  />
                </div>
                <div className="flex gap-3">
                  <button
                    onClick={handleBlockUser}
                    className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
                  >
                    Block User
                  </button>
                  <button
                    onClick={() => {
                      setSelectedUser(null)
                      setBlockUntil('')
                      setBlockReason('')
                    }}
                    className="px-4 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400 transition-colors"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default AdminPanel

