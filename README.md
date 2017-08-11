# Introduction
The project aims at providing Java-based portable software solution for the 2016 - 2019 Czech-Bavarian BCI project. It provides basic 
functionality to support:

- data acquisition (either off-line - from BrainVision files, or on-line - using Lab Streaming Layer API, or BrainVision
RDA API),
- storing the data into a buffer
- segmentation (either into fixed-size segments for continuous data, or EEG marker-based epochs)
- pre-processing (such as channel selection, baseline removal, frequency filtering, and others)
- feature extraction (windowed means, downsampling, and others)
- classification (preferably using methods from deep learning category based on the Deeplearning4j library, such as stacked autoencoders)
- interpretation and evaluation of the results
- graphical user interface to control and configure classification

# Dependencies
Required libraries are handled by Maven. Sample off-line training and testing data based on the Guess the number experiment are a part of the project.

