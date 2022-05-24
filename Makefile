.PHONY: install b2program btypes_primitives btypes_big_integer

b2program:
	./gradlew fatJar

btypes_primitives:
	cd btypes_primitives && ./gradlew fatJar && cp build/libs/btypes_primitives-all.jar ../btypes.jar && cd ..

btypes_big_integer:
	cd btypes_big_integer && ./gradlew fatJar && cp build/libs/btypes_big_integer-all.jar ../btypes.jar && cd ..

refresh:
	./gradlew eclipse --refresh-dependencies

