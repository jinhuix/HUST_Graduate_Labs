import random
import numpy as np

from sim_sycl import SYCL_queue
from sim_sycl import SYCL_memory
from sim_sycl import check_correctness_usm2


if __name__ == "__main__":
    N = 1024
    
    # Create host allocation for data1 and data2
    data1 = SYCL_memory().malloc(N, "int")
    data2 = SYCL_memory().malloc(N)
    
    # Initialize data1 and data2 with random data
    for i in range(len(data1.mem)):
        data1.mem[i] = random.randint(0, 1024)
        data2.mem[i] = random.randint(0, 1024)
    
    # ---------------------------- Begin ----------------------------
    # STEP1 : Create USM device allocation for data1 and data2
    dev_q = SYCL_queue().init_queue_from_device(0, "gpu")
    dev_data1 = SYCL_memory()
    dev_data2 = SYCL_memory()
    
    # STEP2 : Copy data1 and data2 to USM device allocation
    
    # STEP3 : Write kernel code to update data1 on device with square of value, and Run the kernel function on device
    def kernel1(input):
        pass

    # STEP4 : Write kernel code to update data2 on device with twice of value, and run the kernel function on device
    def kernel2(input):
        pass
    
    # STEP5 : Write kernel code to add data2 on device to data1, and run the kernel function on device
    def kernel3(input1, input2):
        pass
    
    # STEP6 : Copy data1 on device to host
    result = SYCL_memory().malloc(N)

    # ---------------------------- End ----------------------------
    # Check Correctness
    check_correctness_usm2(data1, data2, result, dev_q)

    
    # Free memory 
    data1.free()
    data2.free()
    dev_data1.free()
    dev_data2.free()
    result.free()
    