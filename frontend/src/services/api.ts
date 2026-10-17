export interface User { userId: string; userName: string; email: string; account: string }
export interface Product { no: number; productName: string; price: number; feeRate: number }
export interface Like { sn: number; productNo: number; productName: string; price: number; feeRate: number; purchaseQuantity: number; account: string; totalFee: number; totalAmount: number; email: string }
export class ApiError extends Error {
  constructor(public status: number, message: string) { super(message) }
}
let csrf: { token: string; headerName: string } | null = null
export async function api<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  if (method !== 'GET') {
    if (!csrf) {
      const response = await fetch('/api/auth/csrf', { credentials: 'same-origin' })
      if (!response.ok) throw new Error('無法取得安全驗證，請稍後再試')
      csrf = await response.json()
    }
    if (csrf) headers[csrf.headerName] = csrf.token
  }
  let response: Response
  try {
    response = await fetch('/api' + path, { method, headers, credentials: 'same-origin', body: body === undefined ? undefined : JSON.stringify(body) })
  } catch { throw new Error('無法連線至伺服器，請確認服務已啟動') }
  if (!response.ok) {
    const data = await response.json().catch(() => ({ message: '服務暫時無法使用' }))
    if (response.status === 403) csrf = null
    if (response.status === 401 && path !== '/auth/login' && path !== '/auth/me') window.dispatchEvent(new Event('session-expired'))
    throw new ApiError(response.status, data.message || '操作失敗，請稍後再試')
  }
  if (path === '/auth/logout' || path === '/auth/login') csrf = null
  return response.status === 204 ? undefined as T : response.json()
}
export const money = (value: number) => new Intl.NumberFormat('zh-TW', { style: 'currency', currency: 'TWD', minimumFractionDigits: 2 }).format(value)
export const rate = (value: number) => (value * 100).toLocaleString('zh-TW', { maximumFractionDigits: 4 }) + '%'
export const message = (error: unknown) => error instanceof Error ? error.message : '操作失敗，請稍後再試'
