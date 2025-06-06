<template>
  <div class="agent-list-container">
    <div class="cyber-grid"></div>
    <div class="circuit-point point-1"></div>
    <div class="circuit-point point-2"></div>
    <div class="dashboard-header">
      <div class="title-container">
        <h1>⚡ AGENT 控制中心</h1>
        <div class="animated-underline"></div>
      </div>
      <div class="header-actions">
        <button class="create-btn" @click="handleCreate">+ 创建 AGENT</button>
        <!-- <div class="search-bar">
          <input type="text" placeholder="搜索 Agent...">
        </div> -->
      </div>
    </div>

    <div class="table-container">
      <div class="agent-table">
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <span>加载中...</span>
        </div>
        <TransitionGroup name="list" tag="div" v-else>
          <div v-if="agentList.length === 0" :key="'empty'" class="empty-state">
            <div class="empty-content">
              <span class="empty-text">暂无Agent数据</span><br/>
              <button class="create-btn" @click="handleCreate">+ 创建 AGENT</button>
            </div>
          </div>
          
          <div v-for="agent in agentList" 
               :key="agent.id" 
               class="agent-card"
               @click.stop="handleChat(agent)">
            <div class="agent-info">
              <div class="agent-avatar">
                <img v-if="agent.image" 
                     :src="`data:image/jpeg;base64,${agent.image}`" 
                     class="agent-logo" 
                     alt="agent logo"/>
                <div v-else class="agent-logo-placeholder">
                  {{ agent.name.charAt(0).toUpperCase() }}
                </div>
              </div>
              <div class="agent-details">
                <h4 @click.stop="handleShowDetail(agent)">{{agent.name}}</h4>
                <p>{{agent.description}}</p>
              </div>
            </div>

            <div class="agent-status">
              <div class="visibility">
                <span class="badge" :class="{'public': agent.isPublic}">
                  {{agent.isPublic ? '公开' : '私有'}}
                </span>
              </div>
            </div>

            <div class="status" @click.stop>
              <template v-if="agent.instances?.length > 0">
                <el-popover
                  placement="right"
                  trigger="hover"
                  width="auto"
                >
                  <template #reference>
                    <el-tag type="success"><div class="status-tag">runing&nbsp;<el-icon><InfoFilled /></el-icon></div></el-tag>
                  </template>
                  <Instances :agent="agent" />
                </el-popover>
              </template>
              <el-tag v-else type="info">stop</el-tag>
            </div>

            <div class="group">
              <time>{{agent.group}}</time><br/>
              <time>{{agent.version}}</time>
            </div>

            <div class="activity">
              <span>创建时间：</span>
              <time>{{formatDate(agent.ctime)}}</time><br/>
              <span>更新时间：</span>
              <time>{{formatDate(agent.utime)}}</time>
            </div>

            <div class="control-buttons">
              <button class="edit-btn" @click.stop="handleEdit(agent)">编辑</button>
              <button class="delete-btn" @click.stop="handleDelete(agent)">删除</button>
              <button class="task-btn" @click.stop="handleTask(agent)">任务</button>
            </div>
          </div>
        </TransitionGroup>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '创建Agent' : '编辑Agent'"
      width="640px"
    >
      <el-form :model="agentForm" label-width="80px">
        <el-form-item label="名称">
          <el-input 
            v-model="agentForm.name" 
            placeholder="请输入Agent名称"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="agentForm.description" 
            type="textarea" 
            placeholder="请输入Agent描述信息"
          />
        </el-form-item>
        <el-form-item label="Agent URL">
          <el-input 
            v-model="agentForm.agentUrl" 
            placeholder="请输入Agent的URL地址"
          />
        </el-form-item>
        <el-form-item label="公开">
          <el-switch v-model="agentForm.isPublic" />
        </el-form-item>
        <el-form-item label="Logo" class="avatar-uploader-container">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :on-success="handleImageSuccess"
            :on-error="handleImageError"
            accept=".jpg,.jpeg,.png"
          >
            <img v-if="imageUrl" :src="imageUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持 jpg、png 格式，大小不超过 5MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <agent-detail-drawer
      v-model="drawerVisible"
      :agent="selectedAgent"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAgentList, createAgent, updateAgent, deleteAgent } from '@/api/agent'
import type { Agent } from '@/api/agent'
import AgentDetailDrawer from '@/components/AgentDetailDrawer.vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { v4 as uuidv4 } from "uuid";
import Instances from '@/components/Instances.vue'
const agentList = ref<Agent[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref<'create' | 'edit'>('create')
const agentForm = ref({
  id: 0,
  name: '',
  description: '',
  agentUrl: '',
  isPublic: false
})
const drawerVisible = ref(false)
const selectedAgent = ref<Agent | null>(null)
const router = useRouter()
const imageUrl = ref('')
const imageFile = ref<File | null>(null)

const fetchAgents = async () => {
  loading.value = true
  try {
    const response = await getAgentList() 
    if (response.data.code === 200) {
      agentList.value = response.data.data || []
    } else {
      ElMessage.error(response.data.message)
    }
  } catch {
    ElMessage.error('获取Agent列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  dialogType.value = 'create'
  agentForm.value = {
    id: 0,
    name: '',
    description: '',
    agentUrl: '',
    isPublic: false
  }
  imageUrl.value = ''
  imageFile.value = null
  dialogVisible.value = true
}

const handleEdit = (row: Agent) => {
  dialogType.value = 'edit'
  agentForm.value = { ...row }
  if (row.image) {
    imageUrl.value = `data:image/jpeg;base64,${row.image}`
  } else {
    imageUrl.value = ''
  }
  imageFile.value = null
  dialogVisible.value = true
}

const handleDelete = async (row: Agent) => {
  try {
    await ElMessageBox.confirm('确定要删除这个Agent吗？', '提示', {
      type: 'warning'
    })
    const response = await deleteAgent(row.id)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      fetchAgents()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    let imageBase64 = null
    if (imageFile.value) {
      imageBase64 = await new Promise((resolve) => {
        const reader = new FileReader()
        reader.onload = (e) => {
          const base64 = e.target?.result as string
          resolve(base64.split(',')[1])
        }
        reader.readAsDataURL(imageFile.value)
      })
    }

    const formData = {
      name: agentForm.value.name,
      description: agentForm.value.description,
      agentUrl: agentForm.value.agentUrl,
      isPublic: agentForm.value.isPublic,
      image: imageBase64
    }

    if (dialogType.value === 'create') {
      const response = await createAgent(formData)
      if (response.data.code === 200) {
        ElMessage.success('创建成功')
        fetchAgents()
      } else {
        ElMessage.error('创建失败')
      }
    } else {
      const response = await updateAgent(agentForm.value.id, formData)
      if (response.data.code === 200) {
        ElMessage.success('更新成功')
        fetchAgents()
      } else {
        ElMessage.error('更新失败')
      }
    }
    dialogVisible.value = false
    fetchAgents()
  } catch {
    ElMessage.error(dialogType.value === 'create' ? '创建失败' : '更新失败')
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString()
}

const handleShowDetail = (agent: Agent) => {
  selectedAgent.value = agent
  drawerVisible.value = true
}

const handleTask = (agent: Agent) => {
  router.push({
    path: '/tasks',
    query: { serverAgentId: agent.id }
  })
}

const handleChat = (agent: Agent) => {
  router.push({
    path: '/chat',
    query: { serverAgentId: agent.id, conversationId: uuidv4() }
  })
}

const beforeImageUpload = (file: File) => {
  const isValidFormat = ['image/jpeg', 'image/png'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isValidFormat) {
    ElMessage.error('图片格式只能是 JPG 或 PNG!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }

  imageFile.value = file
  imageUrl.value = URL.createObjectURL(file)
  return false
}

const handleImageSuccess = (response: any) => {
  // 处理图片上传成功后的逻辑
}

const handleImageError = (error: any) => {
  // 处理图片上传失败后的逻辑
}

onMounted(() => {
  fetchAgents()
})
</script>

<style scoped>
.agent-list-container {
  min-height: 100vh;
  background: #0d1117;
  color: #fff;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.cyber-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(0, 240, 255, 0.3) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.3) 1px, transparent 1px);
  background-size: 40px 40px;
  animation: gridMove 20s linear infinite;
  transform-origin: center;
  opacity: 0.3;
  pointer-events: none;
}

@keyframes gridMove {
  0% {
    transform: perspective(500px) rotateX(60deg) translateY(0);
  }
  100% {
    transform: perspective(500px) rotateX(60deg) translateY(40px);
  }
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.title-container {
  position: relative;
}

.animated-underline {
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(
    90deg,
    #00f0ff 0%,
    #b400ff 50%,
    #00f0ff 100%
  );
  animation: progressLine 3s ease-in-out infinite;
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.5);
}

@keyframes progressLine {
  0% {
    width: 0;
    opacity: 0.6;
    left: 0;
  }
  50% {
    width: 100%;
    opacity: 1;
    left: 0;
  }
  51% {
    width: 100%;
    opacity: 1;
    left: 0;
  }
  100% {
    width: 0;
    opacity: 0.6;
    left: 100%;
  }
}

.dashboard-header h1 {
  font-family: 'Orbitron', sans-serif;
  font-size: 2rem;
  background: linear-gradient(90deg, #00f0ff, #b400ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-actions {
  display: flex;
  gap: 20px;
  align-items: center;
}

.search-bar {
  position: relative;
  width: 300px;
}

.search-bar input {
  width: 100%;
  padding: 12px 20px;
  background: rgba(13, 17, 23, 0.7);
  border: 1px solid rgba(0, 240, 255, 0.2);
  border-radius: 8px;
  color: #fff;
  font-size: 16px;
}

.scan-animation {
  position: absolute;
  top: 50%;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  animation: scan 2s infinite;
}

.empty-state {
  width: 240px;
  height: 80px;
  margin: 120px auto 0;
  display: flex;
  align-items: center;
}

.empty-state .empty-text {
  display: inline-block;
  margin-bottom: 24px;
  width: 100%;
  text-align: center;
}

.create-btn {
  background: linear-gradient(135deg, #00f0ff, #b400ff);
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  color: #0d1117;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.4);
}

.agent-card {
  background: rgba(13, 17, 23, 0.7);
  border: 1px solid rgba(0, 240, 255, 0.2);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  display: grid;
  grid-template-columns: 4fr 1fr 1fr 1fr 2fr 2fr;
  gap: 20px;
  align-items: center;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  z-index: 1;
}

.agent-card:hover {
  transform: translateX(4px);
  border-color: rgba(0, 240, 255, 0.4);
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.2);
}

.agent-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.agent-avatar {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(135deg, #00f0ff, #b400ff);
}

.agent-logo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.agent-details {
  width: calc(100% - 50px);
}

.agent-logo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: #0d1117;
}

.agent-details h4 {
  font-size: 18px;
  margin-bottom: 5px;
  color: #63f5ff;
  position: relative;
  display: inline-block;
}

.agent-details h4::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #00f0ff, #b400ff);
  transition: width 0.3s ease;
}

.agent-details h4:hover {
  background: linear-gradient(90deg, #00f0ff, #b400ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.agent-details h4:hover::after {
  width: 100%;
}

.agent-details p {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pulse {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #00ff88;
  animation: pulse 2s infinite;
}

.badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.1);
}

.badge.public {
  background: rgba(0, 240, 255, 0.1);
  color: #00f0ff;
  border: 1px solid #00f0ff;
}

.control-buttons {
  display: flex;
  gap: 10px;
}

.control-buttons button {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.edit-btn {
  background: rgba(0, 240, 255, 0.1);
  color: #00f0ff;
  border: 1px solid #00f0ff;
}

.delete-btn {
  background: rgba(255, 85, 85, 0.1);
  color: #ff5555;
  border: 1px solid #ff5555;
}

.task-btn {
  background: rgba(180, 0, 255, 0.1);
  color: #b400ff;
  border: 1px solid #b400ff;
}

@keyframes scan {
  0% { transform: translateY(-10px) scaleX(0); opacity: 0; }
  50% { transform: translateY(0) scaleX(1); opacity: 1; }
  100% { transform: translateY(10px) scaleX(0); opacity: 0; }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .agent-card {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-bar {
    width: 100%;
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #00f0ff;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  margin-bottom: 16px;
  border: 3px solid rgba(0, 240, 255, 0.1);
  border-top: 3px solid #00f0ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.circuit-point {
  position: absolute;
  width: 6px;
  height: 6px;
  background: #00f0ff;
  border-radius: 50%;
  filter: blur(1px);
  box-shadow: 
    0 0 10px #00f0ff,
    0 0 20px #00f0ff,
    0 0 30px rgba(0, 240, 255, 0.5);
  opacity: 0;
  z-index: 0;
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 12px;
    height: 12px;
    background: rgba(0, 240, 255, 0.3);
    border-radius: 50%;
    filter: blur(2px);
  }
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    right: 50%;
    width: 40px;
    height: 2px;
    background: linear-gradient(
      270deg,
      rgba(0, 240, 255, 0.8),
      rgba(0, 240, 255, 0)
    );
    transform-origin: right center;
    transform: translateY(-50%);
    filter: blur(1px);
  }
}

.point-1 {
  bottom: 40px;
  left: 40px;
  animation: movePoint1 15s linear infinite;
  &::after {
    animation: tailRotate1 15s linear infinite;
  }
}

.point-2 {
  bottom: 40px;
  right: 40px;
  animation: movePoint2 15s linear infinite;
  &::after {
    animation: tailRotate2 15s linear infinite;
  }
}

@keyframes movePoint1 {
  0% {
    transform: translate(0, 0);
    opacity: 0;
  }
  5% {
    opacity: 1;
  }
  15% {
    transform: translate(0, -160px);
  }
  30% {
    transform: translate(120px, -160px);
  }
  45% {
    transform: translate(120px, -320px);
  }
  60% {
    transform: translate(240px, -320px);
  }
  75% {
    transform: translate(240px, -480px);
  }
  90% {
    transform: translate(360px, -480px);
    opacity: 1;
  }
  95% {
    opacity: 0;
  }
  100% {
    transform: translate(360px, -480px);
    opacity: 0;
  }
}

@keyframes movePoint2 {
  0% {
    transform: translate(0, 0);
    opacity: 0;
  }
  5% {
    opacity: 1;
  }
  15% {
    transform: translate(0, -200px);
  }
  30% {
    transform: translate(-160px, -200px);
  }
  45% {
    transform: translate(-160px, -360px);
  }
  60% {
    transform: translate(-280px, -360px);
  }
  75% {
    transform: translate(-280px, -520px);
  }
  90% {
    transform: translate(-400px, -520px);
    opacity: 1;
  }
  95% {
    opacity: 0;
  }
  100% {
    transform: translate(-400px, -520px);
    opacity: 0;
  }
}

@keyframes tailRotate1 {
  0%, 15% { transform: translateY(-50%) rotate(-90deg); }
  15.1%, 30% { transform: translateY(-50%) rotate(0deg); }
  30.1%, 45% { transform: translateY(-50%) rotate(-90deg); }
  45.1%, 60% { transform: translateY(-50%) rotate(0deg); }
  60.1%, 75% { transform: translateY(-50%) rotate(-90deg); }
  75.1%, 90% { transform: translateY(-50%) rotate(0deg); }
}

@keyframes tailRotate2 {
  0%, 15% { transform: translateY(-50%) rotate(-90deg); }
  15.1%, 30% { transform: translateY(-50%) rotate(180deg); }
  30.1%, 45% { transform: translateY(-50%) rotate(-90deg); }
  45.1%, 60% { transform: translateY(-50%) rotate(180deg); }
  60.1%, 75% { transform: translateY(-50%) rotate(-90deg); }
  75.1%, 90% { transform: translateY(-50%) rotate(180deg); }
}
</style>
