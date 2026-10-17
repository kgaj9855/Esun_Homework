<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { Like } from '@/services/api'
const props = defineProps<{ item: Like; busy: boolean }>()
const emit = defineEmits<{ close: []; confirm: [] }>()
const dialog = ref<HTMLDialogElement>()
onMounted(() => dialog.value?.showModal())
</script>
<template>
  <dialog ref="dialog" class="favorite-dialog delete-dialog" aria-labelledby="delete-title" @cancel.prevent="!props.busy && emit('close')"><span class="empty-icon">♡</span><h2 id="delete-title">移除這份喜好？</h2><p class="muted">「{{ item.productName }}」將從清單中移除，之後仍可重新收藏。</p><div class="dialog-actions"><button class="button secondary" :disabled="busy" autofocus @click="emit('close')">保留收藏</button><button class="button delete-button" :disabled="busy" @click="emit('confirm')">{{ busy ? '移除中…' : '確認移除' }}</button></div></dialog>
</template>
