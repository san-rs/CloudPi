TARGET = pifft
CC = gcc
SOURCES = pifft.c fftsg.c

all: $(TARGET)

pifft: $(SOURCES)
	$(CC) -o $@ $(SOURCES) -lm

clean:
	rm $(TARGET)
