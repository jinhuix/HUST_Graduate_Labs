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
    
    # STEP2: Create USM shared allocation dev_data in device queue for data
    dev_data = SYCL_memory()
    
    # STEP3: Initialize USM shared allocation
    
    # STEP4: Copy USM shared allocation to host allocation
    
    # STEP5: Write kernel code to update shared data on device with the product of host data and device shared data
    def kernel(input1, input2):
        pass
    
    # STEP6: Run kernel in device queue, wait until the kernel is complete and obtain the result on the host side
    result = SYCL_memory().malloc(N, "int")
    
    # ---------------------------- End ----------------------------
    # Check correctness
    check_correctness_usm1(data, result, dev_q)

    
    # Free memory
    data.free()
    dev_data.free()
    result.free()
    