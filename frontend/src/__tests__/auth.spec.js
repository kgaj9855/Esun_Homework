import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AuthView from '../views/AuthView.vue'
const state = vi.hoisted(() => ({ fail: false, calls: 0, payload: {} }))
vi.mock('@/services/api', () => ({
  api: async (_path, _method, payload) => {
    state.payload = payload
    state.calls++
    if (state.fail) throw new Error('<img src=x onerror=alert(1)>')
    return {}
  },
  message: (e) => e.message,
}))
vi.mock('vue-router', () => ({
  useRoute: () => ({ path: '/register' }),
  useRouter: () => ({ push: vi.fn() }),
}))
describe('registration form', () => {
  beforeEach(() => {
    state.fail = false
    state.calls = 0
  })
  it('submits registration without a member ID or story panel', async () => {
    const view = mount(AuthView, { global: { stubs: { RouterLink: true } } })
    expect(view.find('.auth-story').exists()).toBe(false)
    expect(view.text()).not.toContain('會員 ID')
    for (const input of view.findAll('input[type="password"]'))
      await input.setValue('correct-password')
    await view.find('form').trigger('submit')
    await flushPromises()
    expect(state.calls).toBe(1)
    expect(state.payload).not.toHaveProperty('userId')
    view.unmount()
  })
  it('rejects mismatched password confirmation before making a request', async () => {
    const view = mount(AuthView, { global: { stubs: { RouterLink: true } } })
    await view.findAll('input[type="password"]')[0].setValue('correct-password')
    await view.findAll('input[type="password"]')[1].setValue('different-password')
    await view.find('form').trigger('submit')
    await flushPromises()
    expect(view.get('[role="alert"]').text()).toContain('兩次密碼輸入不一致')
    expect(state.calls).toBe(0)
    view.unmount()
  })
  it('escapes server error messages as text', async () => {
    state.fail = true
    const view = mount(AuthView, { global: { stubs: { RouterLink: true } } })
    for (const input of view.findAll('input[type="password"]'))
      await input.setValue('correct-password')
    await view.find('form').trigger('submit')
    await flushPromises()
    expect(view.get('[role="alert"]').text()).toContain('<img')
    expect(view.find('[role="alert"] img').exists()).toBe(false)
    view.unmount()
  })
})
