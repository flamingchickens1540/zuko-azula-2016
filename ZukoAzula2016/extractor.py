#!/usr/bin/env python3
import tarfile, os, time

chunksiz = 4096

prefix, suffix = "logs-", ".tgz"

for file in os.listdir("."):
	if file.startswith(prefix) and file.endswith(suffix):
		print("Processing:", file)
		name = file[len(prefix):-len(suffix)]
		with tarfile.open(file) as tf:
			for member in tf.getmembers():
				assert member.name.startswith("ccre-storage/log-"), "Bad name: " + member.name
				output_name = "logs/log-%s-%s-%s.txt" % (member.mtime, name, member.name[len("ccre-storage/log-"):])
				print("Extracting:", member.name, "as", output_name)
				reader = tf.extractfile(member)
				if reader:
					try:
						with open(output_name, "wb") as out:
							while True:
								buf = reader.read(chunksiz)
								if not buf: break
								out.write(buf)
					finally:
						reader.close()
		os.remove(file)

print("Done!")
