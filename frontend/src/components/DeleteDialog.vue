<script setup>
import { onMounted, ref } from 'vue'
const props = defineProps({ item: Object, busy: Boolean })
const emit = defineEmits(['close', 'confirm'])
const dialog = ref(null)
function close() {
  if (!props.busy) emit('close')
}
onMounted(() => dialog.value.showModal())
</script>

<template>
  <dialog ref="dialog" aria-labelledby="delete-title" @cancel.prevent="close">
    <h2 id="delete-title">刪除收藏</h2>
    <p>確定刪除「{{ item.productName }}」？</p>
    <div class="actions">
      <button :disabled="busy" @click="emit('close')">取消</button>
      <button :disabled="busy" @click="emit('confirm')">{{ busy ? '刪除中…' : '確認刪除' }}</button>
    </div>
  </dialog>
</template>
