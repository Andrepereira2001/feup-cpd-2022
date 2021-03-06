#include <papi.h>
#include <stdio.h>
#include <time.h>
#include <cstdlib>
#include <iomanip>
#include <iostream>
#include <cstring>
#include <iostream>
#include <fstream> 

using namespace std;

#define SYSTEMTIME clock_t

void OnMult(int m_ar, int m_br) {
  SYSTEMTIME Time1, Time2;

  char st[100];
  double temp;
  int i, j, k;

  double *pha, *phb, *phc;

  pha = (double*)malloc((m_ar * m_ar) * sizeof(double));
  phb = (double*)malloc((m_ar * m_ar) * sizeof(double));
  phc = (double*)malloc((m_ar * m_ar) * sizeof(double));

  for (i = 0; i < m_ar; i++)
    for (j = 0; j < m_ar; j++)
      pha[i * m_ar + j] = (double)1.0;

  for (i = 0; i < m_br; i++)
    for (j = 0; j < m_br; j++)
      phb[i * m_br + j] = (double)(i + 1);

  Time1 = clock();

  for (i = 0; i < m_ar; i++) {
    for (j = 0; j < m_br; j++) {
      temp = 0;
      for (k = 0; k < m_ar; k++) {
        temp += pha[i * m_ar + k] * phb[k * m_br + j];
      }
      phc[i * m_ar + j] = temp;
    }
  }

  Time2 = clock();
  sprintf(st, "Time: %3.3f seconds\n",
          (double)(Time2 - Time1) / CLOCKS_PER_SEC);
  cout << st;

  // display 10 elements of the result matrix tto verify correctness
  cout << "Result matrix: " << endl;
  for (i = 0; i < 1; i++) {
    for (j = 0; j < min(10, m_br); j++)
      cout << phc[j] << " ";
  }
  cout << endl;

  free(pha);
  free(phb);
  free(phc);
}

//different allocation of the matrix in memory
void OnMult2(int m_ar, int m_br) {
  SYSTEMTIME Time1, Time2;

  char st[100];
  double temp;
  int i, j, k;

  double *pha, *phb, *phc;

  pha = (double*)malloc((m_ar * m_ar) * sizeof(double));
  phb = (double*)malloc((m_ar * m_ar) * sizeof(double));
  phc = (double*)malloc((m_ar * m_ar) * sizeof(double));

  for (i = 0; i < m_ar; i++)
    for (j = 0; j < m_ar; j++)
      pha[i * m_ar + j] = (double)1.0;

  //change sequence
  for (i = 0; i < m_br; i++)
    for (j = 0; j < m_br; j++)
      phb[i * m_br + j] = (double)(j + 1); // summing j

  Time1 = clock();

  for (i = 0; i < m_ar; i++) {
    for (j = 0; j < m_br; j++) {
      temp = 0;
      for (k = 0; k < m_ar; k++) {
        temp += pha[i * m_ar + k] * phb[j * m_br + k];
      }
      phc[i * m_ar + j] = temp;
    }
  }

  Time2 = clock();
  sprintf(st, "Time: %3.3f seconds\n",
          (double)(Time2 - Time1) / CLOCKS_PER_SEC);
  cout << st;

  // display 10 elements of the result matrix tto verify correctness
  cout << "Result matrix: " << endl;
  for (i = 0; i < 1; i++) {
    for (j = 0; j < min(10, m_br); j++)
      cout << phc[j] << " ";
  }
  cout << endl;

  free(pha);
  free(phb);
  free(phc);
}

//more efficient because of matrix multiplication (line x line matriz multiplication)
void OnMultLine(int m_ar, int m_br) {
  SYSTEMTIME Time1, Time2;

  char st[100];
  int i, j, k;

  double *pha, *phb, *phc;

  pha = (double*)malloc((m_ar * m_ar) * sizeof(double));
  phb = (double*)malloc((m_ar * m_ar) * sizeof(double));
  phc = (double*)malloc((m_ar * m_ar) * sizeof(double));
  memset(phc, 0, (m_ar * m_ar)*sizeof(double));

  for (i = 0; i < m_ar; i++)
    for (j = 0; j < m_ar; j++)
      pha[i * m_ar + j] = (double)1.0;

  for (i = 0; i < m_br; i++)
    for (j = 0; j < m_br; j++)
      phb[i * m_br + j] = (double)(i + 1);

  Time1 = clock();

  //change sequence
  for (i = 0; i < m_ar; i++) {
    for (k = 0; k < m_ar; k++) {
      for (j = 0; j < m_br; j++) {
        phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
      }
    }
  }

  Time2 = clock();
  sprintf(st, "Time: %3.3f seconds\n",
          (double)(Time2 - Time1) / CLOCKS_PER_SEC);
  cout << st;

  // display 10 elements of the result matrix tto verify correctness
  cout << "Result matrix: " << endl;
  for (i = 0; i < 1; i++) {
    for (j = 0; j < min(10, m_br); j++)
      cout << phc[j] << " ";
  }
  cout << endl;

  free(pha);
  free(phb);
  free(phc);
}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int m_br, int bkSize) {
  SYSTEMTIME Time1, Time2;

  char st[100];
  double temp;
  int i, j, k;
  int x,y,z; 

  double *pha, *phb, *phc;

  pha = (double*)malloc((m_ar * m_ar) * sizeof(double));
  phb = (double*)malloc((m_ar * m_ar) * sizeof(double));
  phc = (double*)malloc((m_ar * m_ar) * sizeof(double));
  memset(phc, 0, (m_ar * m_ar)*sizeof(double));

  for (i = 0; i < m_ar; i++)
    for (j = 0; j < m_ar; j++)
      pha[i * m_ar + j] = (double)1.0;

  for (i = 0; i < m_br; i++)
    for (j = 0; j < m_br; j++)
      phb[i * m_br + j] = (double)(i + 1);

  Time1 = clock();


  for(x=0; x < m_br; x+= bkSize){
    for(z=0; z < m_br; z+= bkSize){
      for(y=0; y < m_br; y+= bkSize){
        for(i = x; i < min(x + bkSize, m_ar); i++){
          for(k = z; k < min(z + bkSize, m_ar); k++){
            for(j = y; j < min(y + bkSize, m_ar); j++){
              phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
            }
          }
        }
      }
    }
  }

  Time2 = clock();
  sprintf(st, "Time: %3.3f seconds\n",
          (double)(Time2 - Time1) / CLOCKS_PER_SEC);
  cout << st;

  // display 10 elements of the result matrix tto verify correctness
  cout << "Result matrix: " << endl;
  for (i = 0; i < 1; i++) {
    for (j = 0; j < min(10, m_br); j++)
      cout << phc[j] << " ";
  }
  cout << endl;

  free(pha);
  free(phb);
  free(phc);
}

void handle_error(int retval) {
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0)
    handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}

int main(int argc, char* argv[]) {
  char c;
  int lin, col, blockSize;
  int op;

  int EventSet = PAPI_NULL;
  long long values[2];
  int ret;
  
  ret = PAPI_library_init(PAPI_VER_CURRENT);
  if (ret != PAPI_VER_CURRENT)
    std::cout << "FAIL" << endl;

  ret = PAPI_create_eventset(&EventSet);
  if (ret != PAPI_OK)
    cout << "ERROR: create eventset" << endl;

  ret = PAPI_add_event(EventSet, PAPI_L1_DCM);
  if (ret != PAPI_OK)
    cout << "ERROR: PAPI_L1_DCM" << endl;

  ret = PAPI_add_event(EventSet, PAPI_L2_DCM);
  if (ret != PAPI_OK)
    cout << "ERROR: PAPI_L2_DCM" << endl;
  

  op = 3;
  col = 4096;
  do {
    cout << endl << endl << "1.1 Multiplication " << col << endl;
    /*cout << "1.2 Multiplication V2" << endl;
    cout << "2. Line Multiplication" << endl;
    cout << "3. Block Multiplication" << endl;
    cout << "Selection?: ";
    cin >> op;
    if (op == 0)
      break;
    printf("Dimensions: lins=cols ? ");*/
    //cin >> lin;

    // Start counting
    ret = PAPI_start(EventSet);
    if (ret != PAPI_OK)
      cout << "ERROR: Start PAPI" << endl;

    switch (op) {
      case 1:
        lin = col;
        OnMult(lin, col);
        break;
      case 12:
        lin = col; 
        OnMult2(lin, col);
        break;
      case 2:
        lin = col; 
        OnMultLine(lin, col);
        break;
      case 3:
        lin = col; 
        blockSize = 128;
        cout << "Block Size" << blockSize << endl;
        OnMultBlock(lin, col, blockSize);
        break;
    }

    ret = PAPI_stop(EventSet, values);
    if (ret != PAPI_OK)
      cout << "ERROR: Stop PAPI" << endl;
    printf("L1 DCM: %lld \n", values[0]);
    printf("L2 DCM: %lld \n", values[1]);

    ret = PAPI_reset(EventSet);
    if (ret != PAPI_OK)
      std::cout << "FAIL reset" << endl;
    
    col+= 2048;

  } while (col <= 10240);

  ret = PAPI_remove_event(EventSet, PAPI_L1_DCM);
  if (ret != PAPI_OK)
    std::cout << "FAIL remove event" << endl;

  ret = PAPI_remove_event(EventSet, PAPI_L2_DCM);
  if (ret != PAPI_OK)
    std::cout << "FAIL remove event" << endl;

  ret = PAPI_destroy_eventset(&EventSet);
  if (ret != PAPI_OK)
    std::cout << "FAIL destroy" << endl;
}