// 所有頁面透過這個方法呼叫後端，登入狀態由 Session Cookie 保存。
export class ApiError extends Error {
  constructor(status, text) {
    super(text)
    this.status = status
  }
}

let csrf = null

export async function api(path, method = 'GET', data) {
  const headers = { Accept: 'application/json' }
  const options = { method, headers, credentials: 'same-origin' }

  if (data !== undefined) {
    headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(data)
  }

  // Spring Security 要求寫入資料時附上 CSRF token。
  if (method !== 'GET') {
    if (csrf === null) {
      let tokenResponse
      try {
        tokenResponse = await fetch('/api/auth/csrf', { credentials: 'same-origin' })
      } catch {
        throw new Error('無法連線至伺服器，請確認服務已啟動')
      }
      if (!tokenResponse.ok) {
        throw new Error('無法取得安全驗證，請重新整理頁面')
      }
      csrf = await tokenResponse.json()
    }
    headers[csrf.headerName] = csrf.token
  }

  let response
  try {
    response = await fetch('/api' + path, options)
  } catch {
    throw new Error('無法連線至伺服器，請確認服務已啟動')
  }

  if (!response.ok) {
    let errorData = { message: '操作失敗，請稍後再試' }
    try {
      errorData = await response.json()
    } catch {
      // 伺服器未回傳 JSON 時，顯示上面的預設訊息。
    }
    if (response.status === 401 || response.status === 403) {
      csrf = null
    }
    if (response.status === 401 && path !== '/auth/login' && path !== '/auth/me') {
      window.dispatchEvent(new Event('session-expired'))
    }
    throw new ApiError(response.status, errorData.message)
  }

  if (path === '/auth/login' || path === '/auth/logout') {
    csrf = null
  }
  if (response.status === 204) {
    return
  }
  return response.json()
}

export function money(value) {
  return Number(value).toLocaleString('zh-TW', {
    style: 'currency',
    currency: 'TWD',
    minimumFractionDigits: 2,
  })
}

export function rate(value) {
  return (Number(value) * 100).toLocaleString('zh-TW', { maximumFractionDigits: 4 }) + '%'
}

export function message(error) {
  if (error instanceof Error) {
    return error.message
  }
  return '操作失敗，請稍後再試'
}
