var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var ChatSchema = new Schema({
	user: Schema.Types.ObjectId,
	group: Schema.Types.ObjectId,
	message: String,
	created: { type: Date, default: Date.now },
});

var Chat = mongoose.model('chat', ChatSchema);

module.exports = Chat;