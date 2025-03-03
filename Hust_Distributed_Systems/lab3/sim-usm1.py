import random
import numpy as np

from sim_sycl import SYCL_queue
from sim_sycl import SYCL_memory
from sim_sycl import check_correctness_usm1

if __name__ == "__main__":
    N = 1024
    # Create host allocation for data
    data = SYCL_memory().malloc(N, "int")
    
    # ---------------------------- Begin ----------------------------
    # STEP1: Create device queue on the gpu-0
    dev_q = SYCL_queue()
    dev_q.init_queue_from_device(0, "gpu")
    
    # STEP2: Create USM shared allocation dev_data in device queue for data
    dev_data = SYCL_memory()
    dev_data.malloc_shared(dev_q, N)
    
    # STEP3: Initialize USM shared allocation
    for i in range(N):
        dev_data.mem[i] = i  # 初始化共享内存中的数据
    
    # STEP4: Copy USM shared allocation to host allocation
    data.copy_from_device(dev_data)  # 将设备数据拷贝到主机内存
    
    # STEP5: Write kernel code to update shared data on device with the product of host data and device shared data
    def kernel(input):
        for i in range(len(input.mem)):
            input.mem[i] = input.mem[i] * data.mem[i]
    
    # STEP6: Run kernel in device queue, wait until the kernel is complete and obtain the result on the host side
    dev_q.parallel_for(kernel, dev_data).wait()  # 在设备上运行核函数
    
    # Allocate result array on host and copy result back
    result = SYCL_memory().malloc(N, "int")
    result.copy_from_device(dev_data)  # 将设备上的结果拷贝回主机
    
    # ---------------------------- End ----------------------------
    # Check correctness
    check_correctness_usm1(data, result, dev_q)

    # Free memory
    data.free()
    dev_data.free()
    result.free()
