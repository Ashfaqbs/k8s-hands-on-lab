- install HEY tool                            
┌──(ashfaq㉿kali-vm)-[~/…/code/k8s/scale-up/rest-api]
└─$ hey -n 10000 -c 10 -m GET https://springboot.local/helloworld --insecure
Command 'hey' not found, but can be installed with:
sudo apt install hey
Do you want to install it? (N/y)y
sudo apt install hey
The following packages were automatically installed and are no longer required:
  fonts-liberation2 libboost-iostreams1.83.0 libgdal34t64 libgfxdr0        libhdf5-hl-100t64 libpoppler134     librdmacm1t64   python3.11         samba-vfs-modules
  ibverbs-providers libboost-thread1.83.0    libgfapi0    libglusterfs0    libibverbs1       libpython3.11-dev libsuperlu6     python3.11-dev
  libarmadillo12    libcephfs2               libgfrpc0    libhdf5-103-1t64 libnetcdf19t64    librados2         python3-lib2to3 python3.11-minimal
Use 'sudo apt autoremove' to remove them.

Installing:
  hey

Summary:
  Upgrading: 0, Installing: 1, Removing: 0, Not Upgrading: 2126
  Download size: 2482 kB
  Space needed: 8022 kB / 4876 MB available

Get:1 http://http.kali.org/kali kali-rolling/main amd64 hey amd64 0.1.4+ds-1+b4 [2482 kB]
Fetched 2482 kB in 4s (697 kB/s)
Selecting previously unselected package hey.
(Reading database ... 441114 files and directories currently installed.)
Preparing to unpack .../hey_0.1.4+ds-1+b4_amd64.deb ...
Unpacking hey (0.1.4+ds-1+b4) ...
Setting up hey (0.1.4+ds-1+b4) ...
Processing triggers for man-db (2.12.1-1) ...
Processing triggers for kali-menu (2023.4.7) ...
Scanning processes...                                                                                                                                                            
Scanning linux images...                                                                                                                                                         

Running kernel seems to be up-to-date.

No services need to be restarted.

No containers need to be restarted.

No user sessions are running outdated binaries.

No VM guests are running outdated hypervisor (qemu) binaries on this host.

                                                                          -Test                                                                                                       
┌──(ashfaq㉿kali-vm)-[~/…/code/k8s/scale-up/rest-api]
└─$ hey -n 10000 -c 10 -m GET https://springboot.local/helloworld --insecure


-n 10000: 10,000 requests.

-c 10: 10 concurrent clients.

--insecure: Ignores self-signed cert.




                        
Summary:
  Total:        56.4085 secs
  Slowest:      0.8265 secs
  Fastest:      0.0003 secs
  Average:      0.0559 secs
  Requests/sec: 177.2782
  
  Total data:   102240 bytes
  Size/request: 10 bytes

Response time histogram:
  0.000 [1]     |
  0.083 [6920]  |■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  0.166 [2474]  |■■■■■■■■■■■■■■
  0.248 [412]   |■■
  0.331 [107]   |■
  0.413 [32]    |
  0.496 [25]    |
  0.579 [5]     |
  0.661 [12]    |
  0.744 [6]     |
  0.826 [6]     |


Latency distribution:
  10% in 0.0025 secs
  25% in 0.0049 secs
  50% in 0.0257 secs
  75% in 0.0912 secs
  90% in 0.1041 secs
  95% in 0.1793 secs
  99% in 0.3038 secs

Details (average, fastest, slowest):
  DNS+dialup:   0.0000 secs, 0.0003 secs, 0.8265 secs
  DNS-lookup:   0.0000 secs, 0.0000 secs, 0.0048 secs
  req write:    0.0000 secs, 0.0000 secs, 0.0122 secs
  resp wait:    0.0557 secs, 0.0003 secs, 0.8264 secs
  resp read:    0.0001 secs, -0.0001 secs, 0.0196 secs

Status code distribution:
  [200] 9984 responses
  [502] 16 responses



                                                                                                                                                                                 
┌──(ashfaq㉿kali-vm)-[~/…/code/k8s/scale-up/rest-api]
└─$ 
                                                          

 
- Watching the events to see scaling

┌──(ashfaq㉿kali-vm)-[~]
└─$ kubectl get hpa -n default --watch
NAME             REFERENCE                          TARGETS       MINPODS   MAXPODS   REPLICAS   AGE
springboot-hpa   Deployment/springboot-deployment   cpu: 2%/50%   1         5         1          8m31s
springboot-hpa   Deployment/springboot-deployment   cpu: 125%/50%   1         5         1          8m31s
springboot-hpa   Deployment/springboot-deployment   cpu: 125%/50%   1         5         3          8m47s
springboot-hpa   Deployment/springboot-deployment   cpu: 64%/50%    1         5         3          9m32s
springboot-hpa   Deployment/springboot-deployment   cpu: 2%/50%     1         5         3          10m
^C                                                                                                                                                                                                                                           
┌──(ashfaq㉿kali-vm)-[~]
└─$ kubectl get hpa -n default --watch
NAME             REFERENCE                          TARGETS       MINPODS   MAXPODS   REPLICAS   AGE
springboot-hpa   Deployment/springboot-deployment   cpu: 2%/50%   1         5         3          13m
springboot-hpa   Deployment/springboot-deployment   cpu: 2%/50%   1         5         3          13m
springboot-hpa   Deployment/springboot-deployment   cpu: 2%/50%   1         5         3          15m
springboot-hpa   Deployment/springboot-deployment   cpu: 2%/50%   1         5         1          15m
^C                                                                                                                                                                                                                                           
┌──(ashfaq㉿kali-vm)-[~]
└─$ kubectl get hpa -n default --watch
NAME             REFERENCE                          TARGETS       MINPODS   MAXPODS   REPLICAS   AGE
springboot-hpa   Deployment/springboot-deployment   cpu: 2%/50%   1         5         1          15m

- we can see its scaling down post testing




- Testing without tool Alternative (Manual):


while true; do curl --insecure https://springboot.local/helloworld; sleep 0.1; done

