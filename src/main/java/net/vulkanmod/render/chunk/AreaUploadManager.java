package net.vulkanmod.render.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.vulkanmod.render.chunk.util.AreaSetQueue;
import net.vulkanmod.vulkan.*;
import net.vulkanmod.vulkan.memory.Buffer;
import net.vulkanmod.vulkan.memory.StagingBuffer;
import net.vulkanmod.vulkan.queue.CommandPool;

import static net.vulkanmod.vulkan.queue.Queue.TransferQueue;
import org.apache.commons.lang3.Validate;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCopy;

import static org.lwjgl.vulkan.VK10.vkWaitForFences;

public class AreaUploadManager {
    public static AreaUploadManager INSTANCE;
    private final Long2ObjectArrayMap<ObjectArrayFIFOQueue<ATst>> DistinctBuffers = new Long2ObjectArrayMap<>(8);

    public static void createInstance() {
        INSTANCE = new AreaUploadManager();
    }

//    ObjectArrayFIFOQueue<ATst>[] recordedUploads;
//    ObjectArrayList<DrawBuffers.ParametersUpdate>[] updatedParameters;
//    ObjectArrayList<Runnable>[] frameOps;
    CommandPool.CommandBuffer[] commandBuffers;

    int currentFrame;

    public void createLists(int frames) {
        this.commandBuffers = new CommandPool.CommandBuffer[frames];
//        this.recordedUploads = new ObjectArrayFIFOQueue[frames];
//        this.updatedParameters = new ObjectArrayList[frames];
//        this.frameOps = new ObjectArrayList[frames];

//        for (int i = 0; i < frames; i++) {
////            this.recordedUploads[i] = new ObjectArrayFIFOQueue<>();
////            this.updatedParameters[i] = new ObjectArrayList<>();
////            this.frameOps[i] = new ObjectArrayList<>();
//        }
    }



    public void submitUploadsAsync(ObjectArrayList<ATst> uploadsed, long id) {
        if(uploadsed.isEmpty())
            return;
        if(commandBuffers[currentFrame] == null)
            this.commandBuffers[currentFrame] = TransferQueue.beginCommands();
        try(MemoryStack stack = MemoryStack.stackPush())
        {
            final int size = uploadsed.size();
            final VkBufferCopy.Buffer copyRegions = VkBufferCopy.malloc(size, stack);
            int i = 0;
//            int rem=0;
//            long src=0;
//            long dst=0;
            for(var copyRegion : copyRegions)
            {
//                var a =this.activeRanges.pop();
                final var virtualSegmentBuffer = uploadsed.get(i);
                copyRegion.srcOffset(virtualSegmentBuffer.offset())
                        .dstOffset(virtualSegmentBuffer.dstOffset())
                        .size(virtualSegmentBuffer.bufferSize());

//                rem+=virtualSegmentBuffer.bufferSize();
//                src=virtualSegmentBuffer.id();
//                dst=virtualSegmentBuffer.bufferId();
                i++;
            }
//            Initializer.LOGGER.info(size+"+"+rem);

            TransferQueue.uploadSuperSet(commandBuffers[currentFrame], copyRegions, Vulkan.getStagingBuffer(currentFrame).getId(), id);
        }
        uploadsed.clear();
    }

    public void submitUploads() {
        Validate.isTrue(currentFrame == Renderer.getCurrentFrame());
        if(this.DistinctBuffers.isEmpty()) return;
        if(commandBuffers[currentFrame] == null)
            this.commandBuffers[currentFrame] = TransferQueue.beginCommands();
        try(MemoryStack stack = MemoryStack.stackPush()) {

            final long l = Vulkan.getStagingBuffer(currentFrame).getId();
            final var itr = DistinctBuffers.values().iterator();
            DistinctBuffers.keySet().forEach(aLong -> {
                var SubBufferSet = itr.next();

                final VkBufferCopy.Buffer copyRegions = VkBufferCopy.malloc(SubBufferSet.size(), stack);
                copyRegions.forEach(vkBufferCopy -> {
                   var a = SubBufferSet.dequeue();
                        vkBufferCopy.srcOffset(a.offset())
                                .dstOffset(a.dstOffset())
                                .size(a.bufferSize());
//                    TransferQueue.uploadBufferCmd(commandBuffers[currentFrame], l, a.offset(), a.bufferId(), a.dstOffset(), a.bufferSize());

                });
                TransferQueue.uploadSuperSet(commandBuffers[currentFrame], copyRegions, l, aLong);


            });
        }
//        for(var id : DistinctBuffers.values()) {
//            id.clear();
//        }
        this.DistinctBuffers.clear();
//        waitUploads();
        this.submit2();
    }

    public void submit2() {
       if(this.commandBuffers[currentFrame]!=null) TransferQueue.submitCommands(this.commandBuffers[currentFrame]);
    }

    public ATst uploadAsync2(long bufferId, long dstOffset, int bufferSize, long src, virtualSegment uploadSegment) {
        Validate.isTrue(currentFrame == Renderer.getCurrentFrame());

        if(commandBuffers[currentFrame] == null)
            this.commandBuffers[currentFrame] = TransferQueue.beginCommands();
//            this.commandBuffers[currentFrame] = GraphicsQueue.getInstance().beginCommands();

        StagingBuffer stagingBuffer = Vulkan.getStagingBuffer(this.currentFrame);
        stagingBuffer.copyBuffer2(bufferSize, src);

//        TransferQueue.uploadBufferCmd(this.commandBuffers[currentFrame], stagingBuffer.getId(), stagingBuffer.getOffset(), bufferId, dstOffset, bufferSize);
        final ATst aTst = new ATst(stagingBuffer.offset, dstOffset, bufferSize, bufferId);
//        this.recordedUploads[this.currentFrame].add(aTst);
        return aTst;
    }
    public void uploadAsync(long bufferId, long dstOffset, int bufferSize, long src, virtualSegment uploadSegment) {
        Validate.isTrue(currentFrame == Renderer.getCurrentFrame());

        if(commandBuffers[currentFrame] == null)
            this.commandBuffers[currentFrame] = TransferQueue.beginCommands();
//            this.commandBuffers[currentFrame] = GraphicsQueue.getInstance().beginCommands();

        StagingBuffer stagingBuffer = Vulkan.getStagingBuffer(this.currentFrame);
        stagingBuffer.copyBuffer2(bufferSize, src);

//        TransferQueue.uploadBufferCmd(this.commandBuffers[currentFrame], stagingBuffer.getId(), stagingBuffer.getOffset(), bufferId, dstOffset, bufferSize);
        final ATst aTst = new ATst(stagingBuffer.offset, dstOffset, bufferSize, bufferId);
//        this.recordedUploads[this.currentFrame].enqueue(aTst);
        if(!this.DistinctBuffers.containsKey(bufferId)) this.DistinctBuffers.put(bufferId, new ObjectArrayFIFOQueue<>());
        this.DistinctBuffers.get(bufferId).enqueue(aTst);
//        return aTst;
    }

    public void enqueueParameterUpdate(DrawBuffers.ParametersUpdate parametersUpdate) {
//        this.updatedParameters[this.currentFrame].add(parametersUpdate);
    }

    public void enqueueFrameOp(Runnable runnable) {
//        this.frameOps[this.currentFrame].add(runnable);
    }

    public void copy(Buffer src, Buffer dst) {
        if(dst.getBufferSize() < src.getBufferSize()) {
            throw new IllegalArgumentException("dst buffer is smaller than src buffer.");
        }

        if(commandBuffers[currentFrame] == null)
            this.commandBuffers[currentFrame] = TransferQueue.beginCommands();

        TransferQueue.uploadBufferCmd(this.commandBuffers[currentFrame], src.getId(), 0, dst.getId(), 0, src.getBufferSize());
    }

    public void updateFrame(int frame) {
        this.currentFrame = frame;
        waitUploads(this.currentFrame);
        executeFrameOps(frame);
    }

    private void executeFrameOps(int frame) {
//        for(DrawBuffers.ParametersUpdate parametersUpdate : this.updatedParameters[frame]) {
//            parametersUpdate.setDrawParameters();
//        }

//        for(Runnable runnable : this.frameOps[frame]) {
//            runnable.run();
//        }
//
////        this.updatedParameters[frame].clear();
//        this.frameOps[frame].clear();
    }

    void waitUploads() {
        this.waitUploads(currentFrame);
    }
    private void waitUploads(int frame) {
        CommandPool.CommandBuffer commandBuffer = commandBuffers[frame];
        if(commandBuffer == null)
            return;
        Synchronization.waitFence(commandBuffers[frame].getFence());

//        for(virtualSegment uploadSegment : this.recordedUploads[frame]) {
//            uploadSegment.setReady();
//        }

//        for(DrawBuffers.ParametersUpdate parametersUpdate : this.updatedParameters[frame]) {
//            parametersUpdate.setDrawParameters();
//        }

        this.commandBuffers[frame].reset();
        this.commandBuffers[frame] = null;
//        this.recordedUploads[frame].clear();
    }

    public void waitAllUploads() {
        for(int i = 0; i < this.commandBuffers.length; ++i) {
            waitUploads(i);
        }
    }

//    public void addToQueue(ObjectArrayList<ATst> uploadsed, long id) {
//        this.recordedUploads[currentFrame].addAll(uploadsed);
//        uploadsed.clear();
//    }
}
